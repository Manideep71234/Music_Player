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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.demo.data.model.Album
import com.example.demo.ui.components.SongItem
import com.example.demo.ui.theme.ActivePillHighlight
import com.example.demo.ui.theme.BackgroundLight
import com.example.demo.ui.theme.TextPrimary
import com.example.demo.ui.theme.TextSecondary
import com.example.demo.ui.theme.getAlbumArtData

/**
 * Album Screen with track list. Tapping a track queues only that album's songs.
 */
@Composable
fun AlbumDetailScreen(
    album: Album,
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    val currentSong by viewModel.currentSong.collectAsState()
    val context = LocalContext.current
    val albumArtData = getAlbumArtData(context, album.albumArtUri)

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
                Text(text = "Album Details", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            // Album Cover & Information Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFE5E7EB)),
                    contentAlignment = Alignment.Center
                ) {
                    if (albumArtData != null) {
                        AsyncImage(
                            model = albumArtData,
                            contentDescription = album.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.clip(RoundedCornerShape(20.dp))
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Album,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = album.title,
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = album.artist,
                        color = TextSecondary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${album.songs.size} tracks",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
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
                        if (album.songs.isNotEmpty()) {
                            viewModel.playSong(album.songs.first(), album.songs)
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
                    Text("Play Album", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        if (album.songs.isNotEmpty()) {
                            viewModel.toggleShuffle()
                            viewModel.playSong(album.songs.shuffled().first(), album.songs)
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

            // Album Tracks List
            LazyColumn(
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(album.songs, key = { it.id }) { song ->
                    SongItem(
                        song = song,
                        isPlaying = currentSong?.id == song.id,
                        onClick = { viewModel.playSong(song, album.songs) },
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
