package com.example.demo.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.demo.data.model.Album
import com.example.demo.data.model.Playlist
import com.example.demo.ui.components.BlurredBottomBar
import com.example.demo.ui.components.MiniPlayer
import com.example.demo.ui.theme.BackgroundLight
import com.example.demo.ui.theme.openSystemEqualizer
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

/**
 * Root Main Scaffold orchestrating root Box, window insets, floating navigation, mini player, and expanded sheets.
 */
@Composable
fun MainScaffold(viewModel: MainViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val shuffleMode by viewModel.shuffleMode.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val queue by viewModel.queue.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val audioSessionId by viewModel.audioSessionId.collectAsState()

    var isFullPlayerExpanded by remember { mutableStateOf(false) }
    var isQueueExpanded by remember { mutableStateOf(false) }
    var selectedPlaylistDetail by remember { mutableStateOf<Playlist?>(null) }
    var selectedAlbumDetail by remember { mutableStateOf<Album?>(null) }

    val hazeState = remember { HazeState() }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // Main Background Content Source for Haze Blur
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState)
        ) {
            if (selectedAlbumDetail != null) {
                AlbumDetailScreen(
                    album = selectedAlbumDetail!!,
                    viewModel = viewModel,
                    onBackClick = { selectedAlbumDetail = null }
                )
            } else if (selectedPlaylistDetail != null) {
                PlaylistDetailScreen(
                    playlist = selectedPlaylistDetail!!,
                    viewModel = viewModel,
                    onBackClick = { selectedPlaylistDetail = null }
                )
            } else {
                LibraryScreen(
                    viewModel = viewModel,
                    onAlbumClick = { album -> selectedAlbumDetail = album },
                    onPlaylistClick = { playlist -> selectedPlaylistDetail = playlist },
                    hazeState = hazeState
                )
            }
        }

        // Overlay MiniPlayer & Floating Blurred Bottom Navigation Bar (Hidden when sheet is expanded)
        if (!isFullPlayerExpanded && !isQueueExpanded) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                MiniPlayer(
                    song = currentSong,
                    isPlaying = isPlaying,
                    currentPosition = currentPosition,
                    duration = duration,
                    hazeState = hazeState,
                    onPlayPauseClick = { viewModel.playOrPause() },
                    onNextClick = { viewModel.skipNext() },
                    onClick = { isFullPlayerExpanded = true }
                )

                if (selectedPlaylistDetail == null && selectedAlbumDetail == null) {
                    BlurredBottomBar(
                        selectedTab = selectedTab,
                        onTabSelected = { viewModel.setSelectedTab(it) },
                        hazeState = hazeState
                    )
                }
            }
        }

        // Animated Full Screen Player Overlay Sheet
        AnimatedVisibility(
            visible = isFullPlayerExpanded,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            FullPlayerSheet(
                song = currentSong,
                isPlaying = isPlaying,
                currentPosition = currentPosition,
                duration = duration,
                shuffleMode = shuffleMode,
                repeatMode = repeatMode,
                onPlayPauseClick = { viewModel.playOrPause() },
                onNextClick = { viewModel.skipNext() },
                onPreviousClick = { viewModel.skipPrevious() },
                onSeek = { viewModel.seekTo(it) },
                onToggleShuffle = { viewModel.toggleShuffle() },
                onToggleRepeat = { viewModel.toggleRepeat() },
                onFavoriteToggle = { currentSong?.let { viewModel.toggleFavorite(it) } },
                onOpenQueue = { isQueueExpanded = true },
                onAddToPlaylist = { viewModel.showAddToPlaylistDialog(currentSong) },
                onOpenEqualizer = { openSystemEqualizer(context, audioSessionId) },
                onDismiss = { isFullPlayerExpanded = false }
            )
        }

        // Animated Queue Screen Overlay
        AnimatedVisibility(
            visible = isQueueExpanded,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            QueueScreen(
                currentSong = currentSong,
                queue = queue,
                currentIndex = currentIndex,
                onQueueItemClick = { viewModel.playQueueItem(it) },
                onRemoveFromQueue = { viewModel.removeFromQueue(it) },
                onReorderQueue = { from, to -> viewModel.reorderQueue(from, to) },
                onClearQueue = { viewModel.clearQueue() },
                onDismiss = { isQueueExpanded = false }
            )
        }
    }
}
