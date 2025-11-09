package com.thugbn.susic.data.model

/**
 * Represents the current playback state of the music player
 */
data class PlaybackState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val playbackSpeed: Float = 1.0f
)

enum class RepeatMode {
    OFF,
    ONE,
    ALL
}
