package com.example.susic

object SusicConstances {
    const val AUDIO = 0
    const val VIDEO = 1
    const val LOADING_DATA_ERROR = R.string.load_metadata_error.toString()
}
enum class StatusEnums {
    LOADING, DONE, ERROR
}

enum class SAttributes {
    PLAYER_DIALOG_OPEN,
    PLAYER_DIALOG_CLOSE,
    AS_TOP_MEDIA,
    LOADING_MEDIA_DATA,
    COMPLETE_LOAD_DATA,

}

enum class PlayerState {
    PAUSE, PLAYING
}