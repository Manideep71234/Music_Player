package com.example.demo.ui.screens

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.demo.data.model.Album
import com.example.demo.data.model.Artist
import com.example.demo.data.model.Playlist
import com.example.demo.data.model.Song
import com.example.demo.data.repository.MusicRepository
import com.example.demo.service.MusicServiceConnection
import com.example.demo.ui.components.LibraryTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Main ViewModel driving library UI, playback, playlists, queue, and mood auto-queue state.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MusicRepository(application)
    private val serviceConnection = MusicServiceConnection.getInstance(application)

    // Permission state
    private val _permissionGranted = MutableStateFlow(false)
    val permissionGranted: StateFlow<Boolean> = _permissionGranted.asStateFlow()

    // Selected Tab & Search Query
    private val _selectedTab = MutableStateFlow(LibraryTab.SONGS)
    val selectedTab: StateFlow<LibraryTab> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Repository Flows
    val songsWithFavorites: StateFlow<List<Song>> = repository.songsWithFavorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rawPlaylists: StateFlow<List<Playlist>> = repository.playlists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered lists based on search query
    val filteredSongs: StateFlow<List<Song>> = combine(
        songsWithFavorites,
        searchQuery
    ) { songs, query ->
        if (query.isBlank()) songs
        else songs.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.artist.contains(query, ignoreCase = true) ||
            it.album.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredAlbums: StateFlow<List<Album>> = combine(
        songsWithFavorites,
        searchQuery
    ) { songs, query ->
        val albums = repository.getAlbums(songs)
        if (query.isBlank()) albums
        else albums.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.artist.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredArtists: StateFlow<List<Artist>> = combine(
        songsWithFavorites,
        searchQuery
    ) { songs, query ->
        val artists = repository.getArtists(songs)
        if (query.isBlank()) artists
        else artists.filter { it.name.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredPlaylists: StateFlow<List<Playlist>> = combine(
        rawPlaylists,
        searchQuery
    ) { playlists, query ->
        if (query.isBlank()) playlists
        else playlists.filter { it.name.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Service Connection Playback State
    val currentSong: StateFlow<Song?> = serviceConnection.currentSong
    val isPlaying: StateFlow<Boolean> = serviceConnection.isPlaying
    val currentPosition: StateFlow<Long> = serviceConnection.currentPosition
    val duration: StateFlow<Long> = serviceConnection.duration
    val shuffleMode: StateFlow<Boolean> = serviceConnection.shuffleMode
    val repeatMode: StateFlow<Int> = serviceConnection.repeatMode
    val audioSessionId: StateFlow<Int> = serviceConnection.audioSessionId
    val queue: StateFlow<List<Song>> = serviceConnection.queue
    val currentIndex: StateFlow<Int> = serviceConnection.currentIndex

    // Playlist Add Dialog state
    private val _songToAddToPlaylist = MutableStateFlow<Song?>(null)
    val songToAddToPlaylist: StateFlow<Song?> = _songToAddToPlaylist.asStateFlow()

    init {
        checkPermissions()
    }

    fun checkPermissions() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        val isGranted = ContextCompat.checkSelfPermission(
            getApplication(),
            permission
        ) == PackageManager.PERMISSION_GRANTED

        if (isGranted) {
            _permissionGranted.value = true
            refreshLibrary()
        }
    }

    fun onPermissionGranted() {
        _permissionGranted.value = true
        refreshLibrary()
    }

    fun refreshLibrary() {
        viewModelScope.launch {
            repository.refreshLibrary()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedTab(tab: LibraryTab) {
        _selectedTab.value = tab
    }

    fun getPlaylistSongs(playlistId: Long): StateFlow<List<Song>> {
        return repository.getPlaylistSongs(playlistId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    // --- Playback Controls ---

    fun playSong(song: Song, playlist: List<Song> = filteredSongs.value) {
        val index = playlist.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        serviceConnection.playSongList(playlist, index)

        viewModelScope.launch {
            val autoQueueSongs = repository.handlePlaySongAutoQueue(song)
            autoQueueSongs.forEach { autoSong ->
                serviceConnection.addToQueue(autoSong)
            }
        }
    }

    fun setSongMood(song: Song, mood: String) {
        viewModelScope.launch {
            repository.setSongMood(song, mood)
        }
    }

    fun playNext(song: Song) {
        serviceConnection.playNext(song)
    }

    fun addToQueue(song: Song) {
        serviceConnection.addToQueue(song)
    }

    fun playOrPause() {
        serviceConnection.playOrPause()
    }

    fun skipNext() {
        serviceConnection.skipToNext()
    }

    fun skipPrevious() {
        serviceConnection.skipToPrevious()
    }

    fun seekTo(positionMs: Long) {
        serviceConnection.seekTo(positionMs)
    }

    fun toggleShuffle() {
        serviceConnection.toggleShuffle()
    }

    fun toggleRepeat() {
        serviceConnection.toggleRepeat()
    }

    fun playQueueItem(index: Int) {
        serviceConnection.playQueueItem(index)
    }

    fun reorderQueue(fromIndex: Int, toIndex: Int) {
        serviceConnection.reorderQueue(fromIndex, toIndex)
    }

    fun removeFromQueue(index: Int) {
        serviceConnection.removeFromQueue(index)
    }

    fun clearQueue() {
        serviceConnection.clearQueue()
    }

    // --- Playlist & Favorites Actions ---

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            repository.toggleFavorite(song.id, song.isFavorite)
        }
    }

    fun createPlaylist(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.createPlaylist(name)
        }
    }

    fun renamePlaylist(playlistId: Long, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            repository.renamePlaylist(playlistId, newName)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
        }
    }

    fun showAddToPlaylistDialog(song: Song?) {
        _songToAddToPlaylist.value = song
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songId)
            _songToAddToPlaylist.value = null
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }
}
