package com.example.demo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.demo.ui.theme.BorderLightGlass
import com.example.demo.ui.theme.SurfaceLightGlass
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

/**
 * Translucent glass box with rounded corners, subtle border, and optional Haze blur.
 */
@Composable
fun GlassContainer(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = SurfaceLightGlass,
    borderColor: Color = BorderLightGlass,
    borderWidth: Dp = 1.dp,
    hazeState: HazeState? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    var containerModifier = modifier.clip(shape)

    if (onClick != null) {
        containerModifier = containerModifier.clickable { onClick() }
    }

    if (hazeState != null) {
        containerModifier = containerModifier.hazeSource(hazeState)
    }

    Box(
        modifier = containerModifier
            .background(backgroundColor, shape)
            .border(borderWidth, borderColor, shape),
        content = content
    )
}
