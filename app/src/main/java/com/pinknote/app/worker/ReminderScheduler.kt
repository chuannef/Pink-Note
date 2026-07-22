package com.pinknote.app.worker

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.pinknote.app.domain.model.Reminder
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun schedule(reminder: Reminder) {
        if (!reminder.enabled) return
        val delayMillis = Duration.between(LocalDateTime.now(), reminder.scheduledAt).toMillis().coerceAtLeast(0)
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    ReminderWorker.KEY_TITLE to reminder.title,
                    ReminderWorker.KEY_MESSAGE to reminder.message,
                    ReminderWorker.KEY_REMINDER_ID to reminder.id
                )
            )
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(reminder.id, ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(reminderId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(reminderId)
    }
}
