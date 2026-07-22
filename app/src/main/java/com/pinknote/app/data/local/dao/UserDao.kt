package com.pinknote.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.pinknote.app.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    fun observeByUid(uid: String): Flow<UserEntity?>

    @Upsert
    suspend fun upsert(user: UserEntity)
}
