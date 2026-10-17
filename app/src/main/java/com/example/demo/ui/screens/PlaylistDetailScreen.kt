package com.example.demo.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.demo.data.model.Playlist
import com.example.demo.ui.components.SongItem
import com.example.demo.ui.theme.ActivePillHighlight
import com.example.demo.ui.theme.BackgroundLight
import com.example.demo.ui.theme.TextPrimary
import com.example.demo.ui.theme.TextSecondary

/**
 * Playlist detail view showing real resolved playlist songs from Room, Play All & Shuffle controls, and remove song options.
 */
@Composable
fun PlaylistDetailScreen(
    playlist: Playlist,
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    val playlistSongs by viewModel.getPlaylistSongs(playlist.id).collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = playlist.name, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(text = "${playlistSongs.size} Songs", color = TextSecondary, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        if (playlistSongs.isNotEmpty()) {
                            viewModel.playSong(playlistSongs.first(), playlistSongs)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = ActivePillHighlight, contentColor = Color.White)
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Play All", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        if (playlistSongs.isNotEmpty()) {
                            viewModel.toggleShuffle()
                            viewModel.playSong(playlistSongs.shuffled().first(), playlistSongs)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6), contentColor = TextPrimary)
                ) {
                    Icon(Icons.Rounded.Shuffle, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Shuffle", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Songs List
            LazyColumn(
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(playlistSongs, key = { it.id }) { song ->
                    SongItem(
                        song = song,
                        isPlaying = currentSong?.id == song.id,
                        onClick = { viewModel.playSong(song, playlistSongs) },
                        onPlayNext = { viewModel.playNext(song) },
                        onAddToQueue = { viewModel.addToQueue(song) },
                        onAddToPlaylist = { viewModel.showAddToPlaylistDialog(song) },
                        onFavoriteToggle = { viewModel.toggleFavorite(song) },
                        onSetMood = { mood -> viewModel.setSongMood(song, mood) }
                    )
                }
            }
        }
    }
}
