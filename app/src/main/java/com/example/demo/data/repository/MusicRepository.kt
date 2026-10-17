package com.example.demo.data.repository

import android.content.Context
import com.example.demo.data.local.FavoriteEntity
import com.example.demo.data.local.MusicDatabase
import com.example.demo.data.local.PlayHistoryEntity
import com.example.demo.data.local.PlaylistEntity
import com.example.demo.data.local.PlaylistSongCrossRef
import com.example.demo.data.local.SongMetaEntity
import com.example.demo.data.model.Album
import com.example.demo.data.model.Artist
import com.example.demo.data.model.Playlist
import com.example.demo.data.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine

class MusicRepository(context: Context) {

    private val scanner = MediaStoreScanner(context)
    private val dao = MusicDatabase.getInstance(context).musicDao()

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    // Flow of favorites updated with local database
    val favoriteSongIds: Flow<List<Long>> = dao.getAllFavoriteSongIds()

    // Combined Flow for songs marked with isFavorite state
    val songsWithFavorites: Flow<List<Song>> = combine(songs, favoriteSongIds) { allSongs, favIds ->
        val favSet = favIds.toSet()
        allSongs.map { song ->
            song.copy(isFavorite = favSet.contains(song.id))
        }
    }

    // Playlists combining entity and songs list
    val playlists: Flow<List<Playlist>> = combine(
        dao.getAllPlaylists(),
        songs
    ) { playlistEntities, _ ->
        playlistEntities.map { entity ->
            Playlist(
                id = entity.id,
                name = entity.name,
                createdAt = entity.createdAt,
                songs = emptyList()
            )
        }
    }

    suspend fun refreshLibrary() {
        _isScanning.value = true
        try {
            val scanned = scanner.scanAudioFiles()
            _songs.value = scanned
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isScanning.value = false
        }
    }

    fun getAlbums(songList: List<Song>): List<Album> {
        return songList.groupBy { it.albumId }.map { (albumId, songsInAlbum) ->
            val firstSong = songsInAlbum.first()
            Album(
                albumId = albumId,
                title = firstSong.album,
                artist = firstSong.artist,
                albumArtUri = firstSong.albumArtUri,
                songCount = songsInAlbum.size,
                songs = songsInAlbum.sortedBy { it.trackNumber }
            )
        }.sortedBy { it.title }
    }

    fun getArtists(songList: List<Song>): List<Artist> {
        return songList.groupBy { it.artist }.map { (artistName, songsByArtist) ->
            val albumCount = songsByArtist.map { it.albumId }.distinct().size
            Artist(
                name = artistName,
                songCount = songsByArtist.size,
                albumsCount = albumCount,
                songs = songsByArtist.sortedBy { it.title }
            )
        }.sortedBy { it.name }
    }

    // --- Playlist & Favorites Actions ---
    suspend fun createPlaylist(name: String): Long {
        return dao.insertPlaylist(PlaylistEntity(name = name))
    }

    suspend fun renamePlaylist(playlistId: Long, newName: String) {
        dao.renamePlaylist(playlistId, newName)
    }

    suspend fun deletePlaylist(playlistId: Long) {
        dao.clearPlaylistSongs(playlistId)
        dao.deletePlaylist(playlistId)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        dao.addSongToPlaylist(PlaylistSongCrossRef(playlistId = playlistId, songId = songId))
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        dao.removeSongFromPlaylist(playlistId, songId)
    }

    fun getPlaylistSongs(playlistId: Long): Flow<List<Song>> {
        return combine(dao.getSongIdsForPlaylist(playlistId), songsWithFavorites) { songIds, allSongs ->
            val songMap = allSongs.associateBy { it.id }
            songIds.mapNotNull { songMap[it] }
        }
    }

    suspend fun toggleFavorite(songId: Long, currentIsFavorite: Boolean) {
        if (currentIsFavorite) {
            dao.removeFavorite(songId)
        } else {
            dao.addFavorite(FavoriteEntity(songId = songId))
        }
    }

    // --- Mood & Language Auto-Queue ---

    suspend fun setSongMood(song: Song, mood: String) {
        val language = LanguageGuesser.guessLanguageFromTitle(song.title)
        dao.saveSongMeta(SongMetaEntity(songId = song.id, mood = mood.lowercase(), language = language))
    }

    suspend fun handlePlaySongAutoQueue(song: Song): List<Song> {
        val language = LanguageGuesser.guessLanguageFromTitle(song.title)

        // Track play history
        dao.insertPlayHistory(PlayHistoryEntity(language = language, timestamp = System.currentTimeMillis()))

        val meta = dao.getSongMeta(song.id)
        if (meta?.mood == "sad") {
            val mostPlayedLang = dao.getMostPlayedLanguageFromHistory() ?: language
            var sadIds = dao.getSongIdsByMoodAndLanguage("sad", mostPlayedLang)
            if (sadIds.isEmpty()) {
                sadIds = dao.getSongIdsByMood("sad")
            }
            val allSongsMap = songs.value.associateBy { it.id }
            return sadIds.filter { it != song.id }.mapNotNull { allSongsMap[it] }
        }

        return emptyList()
    }
}
