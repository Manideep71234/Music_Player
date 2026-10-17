package com.example.demo.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.demo.ui.theme.RoseRed

/**
 * Canvas-rendered 3D Rotating Vinyl Record for songs without album artwork.
 */
@Composable
fun RotatingVinylRecord(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 240.dp
) {
    val rotationAnim = remember { Animatable(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                rotationAnim.animateTo(
                    targetValue = rotationAnim.value + 360f,
                    animationSpec = tween(durationMillis = 3500, easing = LinearEasing)
                )
            }
        } else {
            rotationAnim.stop()
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                rotationZ = rotationAnim.value % 360f
                rotationY = 6f
                cameraDistance = 14f * density
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val outerRadius = size.toPx() / 2f

            // Outer Black Vinyl Disc
            drawCircle(color = Color(0xFF1E1E1E), radius = outerRadius, center = center)

            // Concentric Grooves
            val grooveStep = outerRadius / 14f
            for (i in 3..12) {
                drawCircle(
                    color = Color(0xFF2C2C2C),
                    radius = i * grooveStep,
                    center = center,
                    style = Stroke(width = 1.5f)
                )
            }

            // Inner Label Circle
            drawCircle(color = RoseRed, radius = outerRadius * 0.32f, center = center)

            // Center Spindle Cutout
            drawCircle(color = Color.Black, radius = outerRadius * 0.08f, center = center)
        }
    }
}
