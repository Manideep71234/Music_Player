package com.example.demo.data.local

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * Data relation connecting PlaylistEntity to song IDs via cross reference table.
 */
data class PlaylistWithSongIds(
    @Embedded val playlist: PlaylistEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "songId",
        associateBy = Junction(
            value = PlaylistSongCrossRef::class,
            parentColumn = "playlistId",
            entityColumn = "songId"
        )
    )
    val songIds: List<Long>
)
