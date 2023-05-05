package com.example.susic.player

interface MediaPlayerInterface {
    fun onPositionChanged(position: Int)
    fun onStateChanged()
    fun onClose()
    fun onRepeat(toastMessage: Int)
    fun onStart()
    fun onPause()
    fun onComplete()
}