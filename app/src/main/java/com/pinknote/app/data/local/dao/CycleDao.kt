package com.pinknote.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.pinknote.app.data.local.entity.CycleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CycleDao {
    @Query("SELECT * FROM cycle WHERE uid = :uid LIMIT 1")
    fun observeByUid(uid: String): Flow<CycleEntity?>

    @Upsert
    suspend fun upsert(cycle: CycleEntity)

    @Query("DELETE FROM cycle WHERE uid = :uid")
    suspend fun deleteByUid(uid: String)
}
