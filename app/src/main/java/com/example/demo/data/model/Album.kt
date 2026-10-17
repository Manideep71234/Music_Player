package com.example.demo.data.model

import android.net.Uri

/**
 * Data model representing a music album grouped by album ID.
 */
data class Album(
    val albumId: Long,
    val title: String,
    val artist: String,
    val albumArtUri: Uri?,
    val songCount: Int,
    val songs: List<Song> = emptyList()
)

