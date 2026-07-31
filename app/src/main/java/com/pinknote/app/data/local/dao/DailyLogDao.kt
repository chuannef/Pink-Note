package com.pinknote.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.pinknote.app.data.local.entity.DailyLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyLogDao {
    @Query("SELECT * FROM daily_logs WHERE uid = :uid ORDER BY date DESC")
    fun observeByUid(uid: String): Flow<List<DailyLogEntity>>

    @Query("SELECT * FROM daily_logs WHERE uid = :uid AND date = :date LIMIT 1")
    fun observeByDate(uid: String, date: String): Flow<DailyLogEntity?>

    @Upsert
    suspend fun upsert(log: DailyLogEntity)

    @Query("DELETE FROM daily_logs WHERE uid = :uid")
    suspend fun deleteByUid(uid: String)
}
