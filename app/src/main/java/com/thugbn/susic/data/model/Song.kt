package com.thugbn.susic.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Song entity representing a music track in the local database
 */
@Entity(tableName = "songs")
data class Song(
    @PrimaryKey
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long, // in milliseconds
    val filePath: String,
    val albumArtUri: String? = null,
    val dateAdded: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val lastPlayed: Long? = null
)

