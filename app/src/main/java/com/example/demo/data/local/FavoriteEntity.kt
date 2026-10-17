package com.example.demo.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_songs")
data class FavoriteEntity(
    @PrimaryKey
    val songId: Long
)
