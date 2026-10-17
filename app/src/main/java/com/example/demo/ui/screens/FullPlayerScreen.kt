package com.example.demo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.demo.data.model.Song
import com.example.demo.data.repository.LrcParser
import com.example.demo.ui.components.LyricsView
import com.example.demo.ui.components.RotatingVinylRecord
import com.example.demo.ui.components.formatDuration
import com.example.demo.ui.theme.BackgroundLight
import com.example.demo.ui.theme.FavoriteRed
import com.example.demo.ui.theme.TextPrimary
import com.example.demo.ui.theme.TextSecondary
import com.example.demo.ui.theme.getAlbumArtData
import com.example.demo.ui.theme.rememberAlbumPaletteColors

/**
 * Full Screen Liquid Glass Player with Palette dynamic color adaptation, seek slider, Equalizer button, Material controls, lyrics toggle, and Rotating Vinyl Record fallback.
 */
@Composable
fun FullPlayerScreen(
    song: Song?,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    shuffleMode: Boolean,
    repeatMode: Int,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onOpenQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onDismiss: () -> Unit
) {
    if (song == null) return

    val context = LocalContext.current
    val palette = rememberAlbumPaletteColors(song.albumArtUri)

    var isUserSeeking by remember { mutableStateOf(false) }
    var sliderValue by remember { mutableFloatStateOf(0f) }
    var showLyrics by remember { mutableStateOf(false) }

    val albumArtData = getAlbumArtData(context, song.albumArtUri)
    val lyrics = remember(song) {
        LrcParser.loadLrcForSong(song.title, song.artist, song.dataPath)
    }

    val currentSliderPos = if (isUserSeeking) {
        sliderValue
    } else {
        if (duration > 0) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        palette.vibrantColor.copy(alpha = 0.35f),
                        palette.dominantColor.copy(alpha = 0.20f),
                        BackgroundLight
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Collapse Player",
                        tint = TextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = if (showLyrics) "LYRICS" else "PLAYING FROM LIBRARY",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )

                Row {
                    IconButton(onClick = onOpenEqualizer) {
                        Icon(
                            imageVector = Icons.Rounded.GraphicEq,
                            contentDescription = "Equalizer",
                            tint = TextPrimary
                        )
                    }

                    IconButton(onClick = { showLyrics = !showLyrics }) {
                        Icon(
                            imageVector = Icons.Rounded.Lyrics,
                            contentDescription = "Lyrics",
                            tint = if (showLyrics) palette.vibrantColor else TextSecondary
                        )
                    }

                    IconButton(onClick = onAddToPlaylist) {
                        Icon(
                            imageVector = Icons.Rounded.PlaylistAdd,
                            contentDescription = "Add to Playlist",
                            tint = TextPrimary
                        )
                    }

                    IconButton(onClick = onOpenQueue) {
                        Icon(
                            imageVector = Icons.Rounded.QueueMusic,
                            contentDescription = "Queue",
                            tint = TextPrimary
                        )
                    }
                }
            }

            // Center Content: Large Album Cover, Rotating 3D Vinyl Record Fallback, or Synced Lyrics
            if (showLyrics) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .weight(1f)
                        .padding(vertical = 12.dp)
                ) {
                    LyricsView(
                        lyrics = lyrics,
                        currentPosition = currentPosition,
                        onSeekToLine = onSeek
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color(0xFFF3F4F6)),
                    contentAlignment = Alignment.Center
                ) {
                    if (albumArtData != null) {
                        AsyncImage(
                            model = albumArtData,
                            contentDescription = song.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.clip(RoundedCornerShape(32.dp))
                        )
                    } else {
                        RotatingVinylRecord(
                            isPlaying = isPlaying,
                            size = 250.dp
                        )
                    }
                }
            }

            // Title, Artist & Favorite
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${song.artist} • ${song.album}",
                            color = TextSecondary,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = onFavoriteToggle,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (song.isFavorite) FavoriteRed else TextSecondary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Seek Slider
                Slider(
                    value = currentSliderPos,
                    onValueChange = {
                        isUserSeeking = true
                        sliderValue = it
                    },
                    onValueChangeFinished = {
                        isUserSeeking = false
                        onSeek((sliderValue * duration).toLong())
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = palette.vibrantColor,
                        activeTrackColor = palette.vibrantColor,
                        inactiveTrackColor = Color(0xFFE5E7EB)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val displayPos = if (isUserSeeking) (sliderValue * duration).toLong() else currentPosition
                    Text(text = formatDuration(displayPos), color = TextSecondary, fontSize = 12.sp)
                    Text(text = formatDuration(duration), color = TextSecondary, fontSize = 12.sp)
                }
            }

            // Media Controls Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle Button
                IconButton(onClick = onToggleShuffle) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (shuffleMode) palette.vibrantColor else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Previous
                IconButton(
                    onClick = onPreviousClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = "Previous",
                        tint = TextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Play / Pause Large Button
                IconButton(
                    onClick = onPlayPauseClick,
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(palette.vibrantColor)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }

                // Next
                IconButton(
                    onClick = onNextClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Next",
                        tint = TextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Repeat Mode Button
                IconButton(onClick = onToggleRepeat) {
                    Icon(
                        imageVector = if (repeatMode == 1) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                        contentDescription = "Repeat",
                        tint = if (repeatMode != 0) palette.vibrantColor else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
