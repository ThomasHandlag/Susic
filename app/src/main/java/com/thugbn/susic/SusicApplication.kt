package com.thugbn.susic

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

class SusicApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val notificationManager = getSystemService(NotificationManager::class.java)

        // Music playback notification channel
        val playbackChannel = NotificationChannel(
            PLAYBACK_CHANNEL_ID,
            "Music Playback",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Controls for music playback"
            setShowBadge(false)
        }

        // Sleep timer notification channel
        val timerChannel = NotificationChannel(
            TIMER_CHANNEL_ID,
            "Sleep Timer",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Sleep timer notifications"
        }

        notificationManager.createNotificationChannel(playbackChannel)
        notificationManager.createNotificationChannel(timerChannel)
    }

    companion object {
        const val PLAYBACK_CHANNEL_ID = "music_playback_channel"
        const val TIMER_CHANNEL_ID = "sleep_timer_channel"
    }
}

