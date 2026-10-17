package com.example.demo.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.demo.data.model.Album
import com.example.demo.data.model.Artist
import com.example.demo.data.model.Playlist
import com.example.demo.data.model.Song
import com.example.demo.ui.components.AlbumCard
import com.example.demo.ui.components.ArtistCard
import com.example.demo.ui.components.GlassContainer
import com.example.demo.ui.components.LibraryTab
import com.example.demo.ui.components.PlaylistCard
import com.example.demo.ui.components.SongItem
import com.example.demo.ui.theme.ActivePillHighlight
import com.example.demo.ui.theme.BackgroundLight
import com.example.demo.ui.theme.BorderLightGlass
import com.example.demo.ui.theme.SearchBackground
import com.example.demo.ui.theme.TextMuted
import com.example.demo.ui.theme.TextPrimary
import com.example.demo.ui.theme.TextSecondary
import dev.chrisbanes.haze.HazeState

/**
 * Main Library Screen displaying search bar, tabs (Songs, Albums, Artists, Playlists), and list content with crisp light-glass UI.
 */
@Composable
fun LibraryScreen(
    viewModel: MainViewModel,
    onAlbumClick: (Album) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    hazeState: HazeState? = null,
    modifier: Modifier = Modifier
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val songs by viewModel.filteredSongs.collectAsState()
    val albums by viewModel.filteredAlbums.collectAsState()
    val artists by viewModel.filteredArtists.collectAsState()
    val playlists by viewModel.filteredPlaylists.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()

    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var playlistToRename by remember { mutableStateOf<Playlist?>(null) }
    val songToAddToPlaylist by viewModel.songToAddToPlaylist.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Top Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.setSearchQuery(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Main Content Area
            when (selectedTab) {
                LibraryTab.SONGS -> SongsTabContent(
                    songs = songs,
                    currentSong = currentSong,
                    onSongClick = { song -> viewModel.playSong(song, songs) },
                    onPlayNext = { song -> viewModel.playNext(song) },
                    onAddToQueue = { song -> viewModel.addToQueue(song) },
                    onAddToPlaylist = { song -> viewModel.showAddToPlaylistDialog(song) },
                    onFavoriteToggle = { song -> viewModel.toggleFavorite(song) },
                    onSetMood = { song, mood -> viewModel.setSongMood(song, mood) },
                    onPlayAll = { if (songs.isNotEmpty()) viewModel.playSong(songs.first(), songs) },
                    onShuffleAll = { if (songs.isNotEmpty()) { viewModel.toggleShuffle(); viewModel.playSong(songs.shuffled().first(), songs) } }
                )

                LibraryTab.ALBUMS -> AlbumsTabContent(
                    albums = albums,
                    onAlbumClick = onAlbumClick
                )

                LibraryTab.ARTISTS -> ArtistsTabContent(
                    artists = artists,
                    onArtistClick = { artist ->
                        if (artist.songs.isNotEmpty()) viewModel.playSong(artist.songs.first(), artist.songs)
                    }
                )

                LibraryTab.PLAYLISTS -> PlaylistsTabContent(
                    playlists = playlists,
                    onCreatePlaylistClick = { showCreatePlaylistDialog = true },
                    onPlaylistClick = onPlaylistClick,
                    onRename = { playlistToRename = it },
                    onDelete = { viewModel.deletePlaylist(it.id) }
                )
            }
        }
    }

    // --- Dialogs ---

    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            onConfirm = { name ->
                viewModel.createPlaylist(name)
                showCreatePlaylistDialog = false
            }
        )
    }

    playlistToRename?.let { playlist ->
        RenamePlaylistDialog(
            currentName = playlist.name,
            onDismiss = { playlistToRename = null },
            onConfirm = { newName ->
                viewModel.renamePlaylist(playlist.id, newName)
                playlistToRename = null
            }
        )
    }

    songToAddToPlaylist?.let { song ->
        AddToPlaylistDialog(
            playlists = playlists,
            onDismiss = { viewModel.showAddToPlaylistDialog(null) },
            onSelectPlaylist = { playlistId ->
                viewModel.addSongToPlaylist(playlistId, song.id)
            }
        )
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SearchBackground, CircleShape)
            .border(1.dp, BorderLightGlass, CircleShape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = "Search",
                tint = Color(0xFF6B7280),
                modifier = Modifier.size(20.dp)
            )

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search songs, albums, artists...", color = TextSecondary, fontSize = 14.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = ActivePillHighlight
                ),
                modifier = Modifier.weight(1f)
            )

            AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Clear,
                        contentDescription = "Clear search",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SongsTabContent(
    songs: List<Song>,
    currentSong: Song?,
    onSongClick: (Song) -> Unit,
    onPlayNext: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onFavoriteToggle: (Song) -> Unit,
    onSetMood: (Song, String) -> Unit,
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit
) {
    if (songs.isEmpty()) {
        EmptyState(message = "No songs found")
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 160.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${songs.size} Songs",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Row {
                    Button(
                        onClick = onPlayAll,
                        colors = ButtonDefaults.buttonColors(containerColor = ActivePillHighlight, contentColor = Color.White),
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Play", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onShuffleAll,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6), contentColor = TextPrimary),
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Rounded.Shuffle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Shuffle", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        items(songs, key = { it.id }) { song ->
            SongItem(
                song = song,
                isPlaying = currentSong?.id == song.id,
                onClick = { onSongClick(song) },
                onPlayNext = { onPlayNext(song) },
                onAddToQueue = { onAddToQueue(song) },
                onAddToPlaylist = { onAddToPlaylist(song) },
                onFavoriteToggle = { onFavoriteToggle(song) },
                onSetMood = { mood -> onSetMood(song, mood) }
            )
        }
    }
}

@Composable
fun AlbumsTabContent(
    albums: List<Album>,
    onAlbumClick: (Album) -> Unit
) {
    if (albums.isEmpty()) {
        EmptyState(message = "No albums found")
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(bottom = 160.dp, top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(albums, key = { it.albumId }) { album ->
            AlbumCard(album = album, onClick = { onAlbumClick(album) })
        }
    }
}

@Composable
fun ArtistsTabContent(
    artists: List<Artist>,
    onArtistClick: (Artist) -> Unit
) {
    if (artists.isEmpty()) {
        EmptyState(message = "No artists found")
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 160.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(artists, key = { it.name }) { artist ->
            ArtistCard(artist = artist, onClick = { onArtistClick(artist) })
        }
    }
}

@Composable
fun PlaylistsTabContent(
    playlists: List<Playlist>,
    onCreatePlaylistClick: () -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onRename: (Playlist) -> Unit,
    onDelete: (Playlist) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 160.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Button(
                onClick = onCreatePlaylistClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6), contentColor = TextPrimary)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, tint = TextPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create New Playlist", fontWeight = FontWeight.Bold)
            }
        }

        items(playlists, key = { it.id }) { playlist ->
            PlaylistCard(
                playlist = playlist,
                onClick = { onPlaylistClick(playlist) },
                onRename = { onRename(playlist) },
                onDelete = { onDelete(playlist) }
            )
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 100.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = TextSecondary, fontSize = 15.sp)
    }
}

// --- Dialogs ---

@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Playlist", color = TextPrimary) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Playlist Name") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ActivePillHighlight)
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(text) },
                colors = ButtonDefaults.buttonColors(containerColor = ActivePillHighlight, contentColor = Color.White)
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextMuted) }
        },
        containerColor = BackgroundLight
    )
}

@Composable
fun RenamePlaylistDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Playlist", color = TextPrimary) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ActivePillHighlight)
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(text) },
                colors = ButtonDefaults.buttonColors(containerColor = ActivePillHighlight, contentColor = Color.White)
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextMuted) }
        },
        containerColor = BackgroundLight
    )
}

@Composable
fun AddToPlaylistDialog(
    playlists: List<Playlist>,
    onDismiss: () -> Unit,
    onSelectPlaylist: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Playlist", color = TextPrimary) },
        text = {
            if (playlists.isEmpty()) {
                Text("No playlists created yet.", color = TextMuted)
            } else {
                LazyColumn {
                    items(playlists) { playlist ->
                        Text(
                            text = playlist.name,
                            color = TextPrimary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectPlaylist(playlist.id) }
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextMuted) }
        },
        containerColor = BackgroundLight
    )
}
