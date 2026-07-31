package com.pinknote.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.pinknote.app.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM notifications WHERE uid = :uid ORDER BY scheduledAt ASC")
    fun observeByUid(uid: String): Flow<List<ReminderEntity>>

    @Upsert
    suspend fun upsert(reminder: ReminderEntity)

    @Query("SELECT id FROM notifications WHERE uid = :uid")
    suspend fun getIdsByUid(uid: String): List<String>

    @Query("DELETE FROM notifications WHERE id = :reminderId")
    suspend fun delete(reminderId: String)

    @Query("DELETE FROM notifications WHERE uid = :uid")
    suspend fun deleteByUid(uid: String)
}
