package com.example.demo.data.model

/**
 * Data model representing a music artist grouped by artist name.
 */
data class Artist(
    val name: String,
    val songCount: Int,
    val albumsCount: Int,
    val songs: List<Song> = emptyList()
)
