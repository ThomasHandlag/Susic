package com.example.susic.player

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.session.MediaSession
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.example.susic.MainActivity
import com.example.susic.R

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
        }

        override fun onPause() {
        }

        override fun onSkipToNext() {

        }

        override fun onSkipToPrevious() {

        }

        override fun onStop() {
            mMediaPlayerHolder.stopPlaybackService(
                stopPlayback = true,
                fromUser = true,
                fromFocus = false
            )
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
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE)
            }

        val notification = Notification.Builder(this, MEDIA_SESSION_SERVICE)
            .setContentTitle(getText(R.string.notification_title))
            .setContentText(getText(R.string.notification_message))
            .setSmallIcon(R.drawable.ic_round_person_24)
            .setContentIntent(pendingIntent)
            .setTicker(getText(R.string.ticker_text))
            .build()
        mMediaPlayerHolder.setMusicService(this@PlayerService)
        startForeground(0, notification)
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