package com.example.susic.player

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v4.media.MediaMetadataCompat
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.susic.MainActivity
import com.example.susic.R
import com.example.susic.SusicConstants


class MusicNotificationManager(private val playerService: PlayerService) {

    private val mMediaPlayerHolder get() = MediaPlayerHolder.getInstance()

    private lateinit var sNotificationBuilder: NotificationCompat.Builder
    private val mNotificationManagerCompat get() = NotificationManagerCompat.from(playerService)

    private val mNotificationActions
        @SuppressLint("RestrictedApi")
        get() = sNotificationBuilder.mActions

    private fun getPendingIntent(playerAction: String): PendingIntent {
        val intent = Intent().apply {
            action = playerAction
            component = ComponentName(playerService, PlayerService::class.java)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getService(playerService, 0, intent, flags)
    }

    fun createNotification(onCreated: (Notification) -> Unit) {
        sNotificationBuilder =
            NotificationCompat.Builder(playerService, SusicConstants.CHANNEL_ID)
        val sOpenPlayer = Intent(playerService, MainActivity::class.java)
        sOpenPlayer.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val intent =
            PendingIntent.getActivity(
                playerService,
                SusicConstants.NOF_CODE,
                sOpenPlayer,
                PendingIntent.FLAG_IMMUTABLE
            )
        sNotificationBuilder.setContentIntent(intent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSmallIcon(R.drawable.ic_launcher_round)
            .addAction(getActionNotification(SusicConstants.PLAY_PAUSE))
            .addAction(getActionNotification(SusicConstants.PREV))
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(playerService.getMediaSession()?.sessionToken)
                    .setShowActionsInCompactView(1, 2, 3)
            )
        updateNofContent {
            onCreated(sNotificationBuilder.build())
        }
    }

    fun closePlayerNof() {
        mNotificationManagerCompat.cancel(SusicConstants.NOF_ID)
    }

    fun updatePLayPauseAct() {
        if (::sNotificationBuilder.isInitialized) {
            mNotificationActions[0] = getActionNotification(SusicConstants.PLAY_PAUSE)
        }
    }

    private fun updateNofContent(onDone: (() -> Unit)? = null) {
        mMediaPlayerHolder.getMediaMetaDataCompat()?.run {
            sNotificationBuilder
                .setContentText(getText(MediaMetadataCompat.METADATA_KEY_ARTIST))
                .setContentTitle(getText(MediaMetadataCompat.METADATA_KEY_TITLE))
                .setSubText(getText(MediaMetadataCompat.METADATA_KEY_ALBUM))
                .setLargeIcon(getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART))
        }
        onDone?.invoke()
    }

    private fun getActionNotification(act: String): NotificationCompat.Action {
        val icon = UIHelper.getNotificationActionIC(act)
        return NotificationCompat.Action.Builder(icon, act, getPendingIntent(act)).build()
    }

    fun updateNotification() {
        if (::sNotificationBuilder.isInitialized) {
            sNotificationBuilder.setOngoing(mMediaPlayerHolder.isPlaying)
            updatePLayPauseAct()
            with(mNotificationManagerCompat) {
                if (ActivityCompat.checkSelfPermission(
                        playerService.applicationContext,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                notify(SusicConstants.NOF_ID, sNotificationBuilder.build())
            }
        }
    }
}

object UIHelper {
    @JvmStatic
    fun getNotificationActionIC(act: String): Int {
        val sMediaPlayer = MediaPlayerHolder.getInstance()
        return when (act) {
            SusicConstants.PLAY_PAUSE ->
                if (sMediaPlayer.isPlaying)
                    R.drawable.ic_round_pause_circle_filled_24
                else R.drawable.ic_round_play_arrow_24

            else -> R.drawable.round_close_24
        }
    }
}