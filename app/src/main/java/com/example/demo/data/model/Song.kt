package com.example.demo.data.model

import android.net.Uri

/**
 * Data model representing an audio track retrieved from MediaStore or Room.
 */
data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val duration: Long,
    val contentUri: Uri,
    val albumArtUri: Uri?,
    val dataPath: String = "",
    val trackNumber: Int = 0,
    val isFavorite: Boolean = false
)
