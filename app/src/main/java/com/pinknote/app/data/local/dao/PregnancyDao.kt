package com.pinknote.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.pinknote.app.data.local.entity.PregnancyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PregnancyDao {
    @Query("SELECT * FROM pregnancy WHERE uid = :uid LIMIT 1")
    fun observeByUid(uid: String): Flow<PregnancyEntity?>

    @Upsert
    suspend fun upsert(pregnancy: PregnancyEntity)

    @Query("DELETE FROM pregnancy WHERE uid = :uid")
    suspend fun deleteByUid(uid: String)
}
