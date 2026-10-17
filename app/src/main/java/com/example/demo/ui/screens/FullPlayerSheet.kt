package com.example.demo.ui.screens

import androidx.compose.runtime.Composable
import com.example.demo.data.model.Song

/**
 * FullPlayerSheet wrapper for expanded full player sheet.
 */
@Composable
fun FullPlayerSheet(
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
    FullPlayerScreen(
        song = song,
        isPlaying = isPlaying,
        currentPosition = currentPosition,
        duration = duration,
        shuffleMode = shuffleMode,
        repeatMode = repeatMode,
        onPlayPauseClick = onPlayPauseClick,
        onNextClick = onNextClick,
        onPreviousClick = onPreviousClick,
        onSeek = onSeek,
        onToggleShuffle = onToggleShuffle,
        onToggleRepeat = onToggleRepeat,
        onFavoriteToggle = onFavoriteToggle,
        onOpenQueue = onOpenQueue,
        onAddToPlaylist = onAddToPlaylist,
        onOpenEqualizer = onOpenEqualizer,
        onDismiss = onDismiss
    )
}
