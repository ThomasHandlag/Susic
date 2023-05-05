package com.example.susic.player

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import com.example.susic.MainActivity
import com.example.susic.R


class PlayerService : Service() {

    // Binder given to clients
    private val binder = LocalBinder()

    // Check if is already running
    var isRunning = false

    // Media player
    private val mMediaPlayerHolder get() = MediaPlayerHolder.getInstance()
    lateinit var sNotificationManager: MusicNotificationManager
    var isRestoredFromPause = false

    private var mMediaSessionCompat: MediaSessionCompat? = null

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

    fun getMediaSession(): MediaSessionCompat? = mMediaSessionCompat
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return startId
    }

    override fun onBind(intent: Intent): IBinder {
        synchronized(initializeNotificationManager()) {
            mMediaPlayerHolder.setMusicService(this@PlayerService)
        }
        return binder
    }

    private fun initializeNotificationManager() {
        if (!::sNotificationManager.isInitialized) {
            sNotificationManager = MusicNotificationManager(this)
        }
    }


    inner class LocalBinder : Binder() {
        // Return this instance of PlayerService so we can call public methods
        fun getService(): PlayerService = this@PlayerService
    }

}