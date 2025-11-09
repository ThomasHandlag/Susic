package com.thugbn.susic.data.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * Data class representing a playlist with its songs
 */
data class PlaylistWithSongs(
    @Embedded val playlist: Playlist,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PlaylistSongCrossRef::class,
            parentColumn = "playlistId",
            entityColumn = "songId"
        )
    )
    val songs: List<Song>
)

