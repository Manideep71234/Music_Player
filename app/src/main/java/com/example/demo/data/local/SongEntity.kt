package com.example.demo.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val duration: Long,
    val contentUriString: String,
    val albumArtUriString: String?,
    val dataPath: String,
    val trackNumber: Int = 0
)
