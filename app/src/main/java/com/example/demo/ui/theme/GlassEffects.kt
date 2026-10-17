package com.example.demo.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlin.math.abs

/**
 * Helper modifiers for liquid glass visuals and dynamic album art gradients.
 */

@Composable
fun Modifier.glassSurface(
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    backgroundColor: Color = SurfaceLightGlass,
    borderColor: Color = BorderLightGlass,
    borderWidth: Dp = 1.dp,
    hazeState: HazeState? = null
): Modifier {
    var modifier = this.clip(shape)
    if (hazeState != null) {
        modifier = modifier.hazeSource(hazeState)
    }
    return modifier
        .background(backgroundColor, shape)
        .border(borderWidth, borderColor, shape)
}

/**
 * Derives a vibrant soft gradient pair from a song or album ID to create album art ambient glow.
 */
fun getAmbientGradientFromId(id: Long): Pair<Color, Color> {
    val seed = abs(id.hashCode())
    val hues = listOf(
        Pair(Color(0xFFE8ECF8), Color(0xFFF3F4F6)),
        Pair(Color(0xFFFDF2F8), Color(0xFFF3E8FF)),
        Pair(Color(0xFFECFDF5), Color(0xFFE0F2FE)),
        Pair(Color(0xFFFFFBEB), Color(0xFFFEF2F2)),
        Pair(Color(0xFFF3F4F6), Color(0xFFE5E7EB))
    )
    return hues[seed % hues.size]
}

val DefaultSpringAnimationSpec = tween<Float>(durationMillis = 350, easing = FastOutSlowInEasing)
