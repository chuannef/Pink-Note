package com.pinknote.app.data.repository

import com.pinknote.app.data.local.dao.ReminderDao
import com.pinknote.app.data.remote.firebase.FirebaseDataSource
import com.pinknote.app.domain.model.Reminder
import com.pinknote.app.domain.repository.ReminderRepository
import com.pinknote.app.worker.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepositoryImpl @Inject constructor(
    private val reminderDao: ReminderDao,
    private val firebaseDataSource: FirebaseDataSource,
    private val reminderScheduler: ReminderScheduler
) : ReminderRepository {
    override fun observeReminders(uid: String): Flow<List<Reminder>> {
        return reminderDao.observeByUid(uid).map { reminders -> reminders.map { it.toDomain() } }
    }

    override suspend fun saveReminder(reminder: Reminder) {
        reminderDao.upsert(reminder.toEntity())
        runCatching { firebaseDataSource.saveReminder(reminder) }
        scheduleReminder(reminder)
    }

    override suspend fun scheduleReminder(reminder: Reminder) {
        reminderScheduler.schedule(reminder)
    }

    override suspend fun cancelReminder(reminderId: String) {
        reminderScheduler.cancel(reminderId)
        reminderDao.delete(reminderId)
    }
}
