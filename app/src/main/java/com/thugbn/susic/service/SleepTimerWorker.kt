package com.thugbn.susic.service

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.common.util.concurrent.MoreExecutors
import com.thugbn.susic.R
import com.thugbn.susic.SusicApplication
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Worker that pauses music playback when sleep timer expires
 */
class SleepTimerWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            pausePlayback()
            showTimerExpiredNotification()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun pausePlayback() {
        val sessionToken = SessionToken(
            applicationContext,
            ComponentName(applicationContext, MusicPlaybackService::class.java)
        )

        val controllerFuture = MediaController.Builder(applicationContext, sessionToken)
            .buildAsync()

        suspendCancellableCoroutine { continuation ->
            controllerFuture.addListener({
                try {
                    val controller = controllerFuture.get()
                    controller.pause()
                    controller.release()
                    continuation.resume(Unit)
                } catch (e: Exception) {
                    continuation.resume(Unit)
                }
            }, MoreExecutors.directExecutor())
        }
    }

    private fun showTimerExpiredNotification() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager

        val notification = NotificationCompat.Builder(
            applicationContext,
            SusicApplication.TIMER_CHANNEL_ID
        )
            .setSmallIcon(androidx.media3.session.R.drawable.media3_icon_radio)
            .setContentTitle("Sleep Timer")
            .setContentText("Sleep timer expired. Playback paused.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
    }
}


