package com.example.susic

object SusicConstants {
    const val AUDIO = 0
    const val VIDEO = 1
    const val LOADING_DATA_ERROR = R.string.load_metadata_error.toString()
    const val CHANNEL_ID = "PLAYER_NOTIFICATION"
    const val NOF_CODE = 100
    const val PLAY_PAUSE = "PLAY_PAUSE"
    const val PREV = "PREVIOUS"
    const val NEXT = "NEXT"
    const val CLOSE = "CLOSE"
    const val NOF_ID = 101
}
enum class StatusEnums {
    LOADING, DONE, ERROR, EMPTY
}



enum class PlayerState {
    PAUSE, PLAYING, IDLE
}