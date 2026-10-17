package com.example.demo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState

enum class LibraryTab(val title: String, val icon: ImageVector) {
    SONGS("Songs", Icons.Rounded.LibraryMusic),
    ALBUMS("Albums", Icons.Rounded.Album),
    ARTISTS("Artists", Icons.Rounded.Person),
    PLAYLISTS("Playlists", Icons.Rounded.QueueMusic)
}

val RoseRed = Color(0xFFE11D48)
val RoseDroplet = Color(0xFFFB7185).copy(alpha = 0.20f)
val SlateGray = Color(0xFF94A3B8)

/**
 * Floating frosted-glass navigation pill with "Water Drop" liquid spring tab selector animation.
 */
@Composable
fun BlurredBottomBar(
    selectedTab: LibraryTab,
    onTabSelected: (LibraryTab) -> Unit,
    hazeState: HazeState? = null,
    modifier: Modifier = Modifier
) {
    val tabs = LibraryTab.entries

    GlassContainer(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp)
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                spotColor = Color(0x1A000000)
            ),
        shape = CircleShape,
        backgroundColor = Color(0xFFFFFFFF).copy(alpha = 0.65f),
        borderColor = Color.White.copy(alpha = 0.5f),
        borderWidth = 1.dp,
        hazeState = hazeState
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val isSelected = selectedTab == tab

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTabSelected(tab) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .width(64.dp)
                                .height(42.dp)
                                .clip(RoundedCornerShape(21.dp))
                                .background(RoseDroplet)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = if (isSelected) RoseRed else SlateGray,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )

                        Text(
                            text = tab.title,
                            color = if (isSelected) RoseRed else SlateGray,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
