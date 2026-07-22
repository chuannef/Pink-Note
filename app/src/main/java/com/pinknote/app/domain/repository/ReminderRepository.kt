package com.pinknote.app.domain.repository

import com.pinknote.app.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun observeReminders(uid: String): Flow<List<Reminder>>
    suspend fun saveReminder(reminder: Reminder)
    suspend fun scheduleReminder(reminder: Reminder)
    suspend fun cancelReminder(reminderId: String)
}
