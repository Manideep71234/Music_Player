package com.example.demo.service

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.demo.data.model.Song
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Singleton connection manager connecting UI ViewModels to Media3 MediaController.
 */
class MusicServiceConnection private constructor(context: Context) {

    private val applicationContext = context.applicationContext
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressUpdateJob: Job? = null

    // Player State Flows
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _shuffleMode = MutableStateFlow(false)
    val shuffleMode: StateFlow<Boolean> = _shuffleMode.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _audioSessionId = MutableStateFlow(0)
    val audioSessionId: StateFlow<Int> = _audioSessionId.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val songMap = mutableMapOf<String, Song>()

    init {
        connectService()
    }

    private fun connectService() {
        val sessionToken = SessionToken(
            applicationContext,
            ComponentName(applicationContext, MusicService::class.java)
        )
        controllerFuture = MediaController.Builder(applicationContext, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                mediaController = controllerFuture?.get()
                setupPlayerListener()
                updatePlayerState()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())
    }

    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    startProgressTracker()
                } else {
                    stopProgressTracker()
                }
            }

            @OptIn(UnstableApi::class)
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                _audioSessionId.value = audioSessionId
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateCurrentSong(mediaItem)
                updateQueueState()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                updatePlayerState()
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _shuffleMode.value = shuffleModeEnabled
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                _repeatMode.value = repeatMode
            }
        })
    }

    private fun startProgressTracker() {
        progressUpdateJob?.cancel()
        progressUpdateJob = scope.launch {
            while (isActive) {
                mediaController?.let { controller ->
                    _currentPosition.value = controller.currentPosition.coerceAtLeast(0L)
                    _duration.value = controller.duration.coerceAtLeast(0L)
                }
                delay(500)
            }
        }
    }

    private fun stopProgressTracker() {
        progressUpdateJob?.cancel()
    }

    private fun updatePlayerState() {
        mediaController?.let { controller ->
            _isPlaying.value = controller.isPlaying
            _shuffleMode.value = controller.shuffleModeEnabled
            _repeatMode.value = controller.repeatMode
            _currentPosition.value = controller.currentPosition.coerceAtLeast(0L)
            _duration.value = controller.duration.coerceAtLeast(0L)
            updateCurrentSong(controller.currentMediaItem)
            updateQueueState()
        }
    }

    private fun updateCurrentSong(mediaItem: MediaItem?) {
        mediaItem?.let { item ->
            val idStr = item.mediaId
            val song = songMap[idStr] ?: parseSongFromMediaItem(item)
            _currentSong.value = song
        } ?: run {
            _currentSong.value = null
        }
    }

    private fun updateQueueState() {
        mediaController?.let { controller ->
            val items = mutableListOf<Song>()
            val itemCount = controller.mediaItemCount
            for (i in 0 until itemCount) {
                val item = controller.getMediaItemAt(i)
                val song = songMap[item.mediaId] ?: parseSongFromMediaItem(item)
                items.add(song)
            }
            _queue.value = items
            _currentIndex.value = controller.currentMediaItemIndex
        }
    }

    private fun parseSongFromMediaItem(mediaItem: MediaItem): Song {
        val metadata = mediaItem.mediaMetadata
        val id = mediaItem.mediaId.toLongOrNull() ?: 0L
        return Song(
            id = id,
            title = metadata.title?.toString() ?: "Unknown",
            artist = metadata.artist?.toString() ?: "Unknown",
            album = metadata.albumTitle?.toString() ?: "Unknown",
            albumId = 0L,
            duration = 0L,
            contentUri = mediaItem.localConfiguration?.uri ?: Uri.EMPTY,
            albumArtUri = metadata.artworkUri
        )
    }

    private fun songToMediaItem(song: Song): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artist)
            .setAlbumTitle(song.album)
            .setArtworkUri(song.albumArtUri)
            .setExtras(Bundle().apply {
                putLong("duration", song.duration)
                putLong("albumId", song.albumId)
            })
            .build()

        return MediaItem.Builder()
            .setMediaId(song.id.toString())
            .setUri(song.contentUri)
            .setMediaMetadata(metadata)
            .build()
    }

    // --- Actions ---

    fun playSongList(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) return
        songs.forEach { songMap[it.id.toString()] = it }

        val mediaItems = songs.map { songToMediaItem(it) }
        mediaController?.let { controller ->
            controller.setMediaItems(mediaItems, startIndex.coerceIn(0, songs.size - 1), 0L)
            controller.prepare()
            controller.play()
        }
    }

    fun playNext(song: Song) {
        songMap[song.id.toString()] = song
        mediaController?.let { controller ->
            val nextIndex = (controller.currentMediaItemIndex + 1).coerceAtMost(controller.mediaItemCount)
            controller.addMediaItem(nextIndex, songToMediaItem(song))
            updateQueueState()
        }
    }

    fun addToQueue(song: Song) {
        songMap[song.id.toString()] = song
        mediaController?.let { controller ->
            controller.addMediaItem(songToMediaItem(song))
            updateQueueState()
        }
    }

    fun playOrPause() {
        mediaController?.let { controller ->
            if (controller.isPlaying) {
                controller.pause()
            } else {
                if (controller.playbackState == Player.STATE_ENDED) {
                    controller.seekTo(0, 0L)
                }
                controller.play()
            }
        }
    }

    fun skipToNext() {
        mediaController?.let { controller ->
            if (controller.hasNextMediaItem()) {
                controller.seekToNextMediaItem()
            }
        }
    }

    fun skipToPrevious() {
        mediaController?.let { controller ->
            if (controller.currentPosition > 3000) {
                controller.seekTo(0L)
            } else if (controller.hasPreviousMediaItem()) {
                controller.seekToPreviousMediaItem()
            } else {
                controller.seekTo(0L)
            }
        }
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
        _currentPosition.value = positionMs
    }

    fun toggleShuffle() {
        mediaController?.let { controller ->
            val nextShuffle = !controller.shuffleModeEnabled
            controller.shuffleModeEnabled = nextShuffle
            _shuffleMode.value = nextShuffle
        }
    }

    fun toggleRepeat() {
        mediaController?.let { controller ->
            val nextRepeat = when (controller.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
            controller.repeatMode = nextRepeat
            _repeatMode.value = nextRepeat
        }
    }

    fun playQueueItem(index: Int) {
        mediaController?.let { controller ->
            if (index in 0 until controller.mediaItemCount) {
                controller.seekTo(index, 0L)
                controller.play()
            }
        }
    }

    fun reorderQueue(fromIndex: Int, toIndex: Int) {
        mediaController?.let { controller ->
            if (fromIndex in 0 until controller.mediaItemCount && toIndex in 0 until controller.mediaItemCount) {
                controller.moveMediaItem(fromIndex, toIndex)
                updateQueueState()
            }
        }
    }

    fun removeFromQueue(index: Int) {
        mediaController?.let { controller ->
            if (index in 0 until controller.mediaItemCount) {
                controller.removeMediaItem(index)
                updateQueueState()
            }
        }
    }

    fun clearQueue() {
        mediaController?.let { controller ->
            controller.clearMediaItems()
            _queue.value = emptyList()
            _currentSong.value = null
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: MusicServiceConnection? = null

        fun getInstance(context: Context): MusicServiceConnection {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MusicServiceConnection(context).also { INSTANCE = it }
            }
        }
    }
}
