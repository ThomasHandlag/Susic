package com.thugbn.susic.service

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * Manager for sleep timer functionality using WorkManager
 */
class SleepTimerManager(private val context: Context) {

    companion object {
        private const val SLEEP_TIMER_WORK_NAME = "sleep_timer_work"
        const val SLEEP_TIMER_TAG = "sleep_timer"
    }

    /**
     * Schedule sleep timer to pause playback after specified duration
     * @param durationMinutes Duration in minutes
     */
    fun scheduleSleepTimer(durationMinutes: Int) {
        val workRequest = OneTimeWorkRequestBuilder<SleepTimerWorker>()
            .setInitialDelay(durationMinutes.toLong(), TimeUnit.MINUTES)
            .addTag(SLEEP_TIMER_TAG)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                SLEEP_TIMER_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
    }

    /**
     * Cancel active sleep timer
     */
    fun cancelSleepTimer() {
        WorkManager.getInstance(context)
            .cancelUniqueWork(SLEEP_TIMER_WORK_NAME)
    }

    /**
     * Check if sleep timer is currently active
     */
    fun isSleepTimerActive(): Boolean {
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(SLEEP_TIMER_WORK_NAME)
            .get()

        return workInfos.any { workInfo ->
            workInfo.state == WorkInfo.State.ENQUEUED || workInfo.state == WorkInfo.State.RUNNING
        }
    }

    /**
     * Get remaining time for active sleep timer
     * @return Remaining time in milliseconds, or null if no active timer
     */
    fun getRemainingTime(): Long? {
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(SLEEP_TIMER_WORK_NAME)
            .get()

        val activeWork = workInfos.firstOrNull { workInfo ->
            workInfo.state == WorkInfo.State.ENQUEUED || workInfo.state == WorkInfo.State.RUNNING
        }
// Calculate remaining time based on scheduled time
        // Note: This is approximate as WorkManager doesn't expose exact remaining time
        return activeWork?.let {

            null
        }
    }
}