package com.thugbn.susic.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Playlist entity
 */
@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val coverImageUri: String? = null
)

