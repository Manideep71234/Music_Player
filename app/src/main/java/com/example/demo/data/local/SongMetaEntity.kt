package com.example.demo.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "song_meta")
data class SongMetaEntity(
    @PrimaryKey
    val songId: Long,
    val mood: String, // e.g. "sad", "happy", "energetic", "calm"
    val language: String // e.g. "English", "Hindi", "Asian", "Spanish"
)
