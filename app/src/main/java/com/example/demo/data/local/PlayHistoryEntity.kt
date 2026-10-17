package com.example.demo.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "play_history")
data class PlayHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val language: String,
    val timestamp: Long = System.currentTimeMillis()
)
