package com.example.demo.data.model

/**
 * Domain model representing a playlist.
 */
data class Playlist(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val songs: List<Song> = emptyList()
)
