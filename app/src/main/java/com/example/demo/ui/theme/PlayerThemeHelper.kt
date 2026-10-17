package com.example.demo.ui.theme

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AlbumPaletteColors(
    val vibrantColor: Color = Color(0xFFFB7185),
    val dominantColor: Color = Color(0xFF1E293B),
    val darkMutedColor: Color = Color(0xFF0F172A)
)

/**
 * Extracts palette colors asynchronously from album artwork bitmap and returns animated transitions.
 */
@Composable
fun rememberAlbumPaletteColors(albumArtUri: Uri?): AlbumPaletteColors {
    val context = LocalContext.current
    var targetColors by remember { mutableStateOf(AlbumPaletteColors()) }

    LaunchedEffect(albumArtUri) {
        if (albumArtUri != null) {
            targetColors = extractPaletteColors(context, albumArtUri)
        } else {
            targetColors = AlbumPaletteColors()
        }
    }

    val animatedVibrant by animateColorAsState(targetColors.vibrantColor, animationSpec = tween(600), label = "vibrant")
    val animatedDominant by animateColorAsState(targetColors.dominantColor, animationSpec = tween(600), label = "dominant")
    val animatedDarkMuted by animateColorAsState(targetColors.darkMutedColor, animationSpec = tween(600), label = "darkMuted")

    return AlbumPaletteColors(
        vibrantColor = animatedVibrant,
        dominantColor = animatedDominant,
        darkMutedColor = animatedDarkMuted
    )
}

suspend fun extractPaletteColors(context: Context, uri: Uri): AlbumPaletteColors = withContext(Dispatchers.IO) {
    try {
        val bitmap = loadBitmapForPalette(context, uri) ?: return@withContext AlbumPaletteColors()
        val palette = Palette.from(bitmap).generate()

        val vibrant = palette.getVibrantColor(0xFFE11D48.toInt())
        val dominant = palette.getDominantColor(0xFF1E293B.toInt())
        val darkMuted = palette.getDarkMutedColor(0xFF0F172A.toInt())

        AlbumPaletteColors(
            vibrantColor = Color(vibrant),
            dominantColor = Color(dominant),
            darkMutedColor = Color(darkMuted)
        )
    } catch (e: Exception) {
        AlbumPaletteColors()
    }
}

private fun loadBitmapForPalette(context: Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver.loadThumbnail(uri, Size(256, 256), null)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    } catch (e: Exception) {
        null
    }
}
