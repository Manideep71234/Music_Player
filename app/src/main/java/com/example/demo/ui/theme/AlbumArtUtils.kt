package com.example.demo.ui.theme

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Size

/**
 * Utility to load album art thumbnails using ContentResolver.loadThumbnail on API 29+ with fallback to Uri.
 */
fun getAlbumArtData(context: Context, uri: Uri?): Any? {
    if (uri == null) return null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        return try {
            context.contentResolver.loadThumbnail(uri, Size(512, 512), null)
        } catch (e: Exception) {
            uri
        }
    }
    return uri
}
