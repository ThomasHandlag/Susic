package com.example.susic.player

import android.app.Service
import android.content.Intent
import android.media.session.MediaSession
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat

private const val WAKELOCK_MILLI: Long = 25000

private const val DOUBLE_CLICK = 400

class PlayerService : Service() {

    // Binder given to clients
    private val binder = LocalBinder()

    // Check if is already running
    var isRunning = false

    // Media player
    private val mMediaPlayerHolder get() = MediaPlayerHolder.getInstance()
    lateinit var musicNotificationManager: MusicNotificationManager
    var isRestoredFromPause = false

    private var mMediaSessionCompat: MediaSessionCompat? = null

    private val mMediaSessionCallback = object : MediaSessionCompat.Callback() {

        override fun onPlay() {
            mMediaPlayerHolder.resumeOrPause()
        }

        override fun onPause() {
            mMediaPlayerHolder.resumeOrPause()
        }

        override fun onSkipToNext() {

        }

        override fun onSkipToPrevious() {

        }

        override fun onStop() {
            mMediaPlayerHolder.stopPlaybackService(stopPlayback = true, fromUser = true, fromFocus = false)
        }

        override fun onSeekTo(pos: Long) {
            mMediaPlayerHolder.seekTo(
                pos.toInt(),
            )
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        mMediaSessionCompat?.run {
            if (isActive) {
                isActive = false
                setCallback(null)
                release()
            }
        }

        mMediaSessionCompat = null
        mMediaPlayerHolder.release()
        isRunning = false
    }

    override fun onCreate() {

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isRunning = true

        try {
            intent?.action?.let { act ->

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return startId
    }

    override fun onBind(intent: Intent): IBinder {
        synchronized(initializeNotificationManager()) {
            mMediaPlayerHolder.setMusicService(this@PlayerService)
        }
        return binder
    }

    private fun initializeNotificationManager() {
        if (!::musicNotificationManager.isInitialized) {
            musicNotificationManager = MusicNotificationManager(this)
        }
    }


    inner class LocalBinder : Binder() {
        // Return this instance of PlayerService so we can call public methods
        fun getService(): PlayerService = this@PlayerService
    }

}