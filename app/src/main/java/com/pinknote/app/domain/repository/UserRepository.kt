package com.pinknote.app.domain.repository

import com.pinknote.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeProfile(uid: String): Flow<UserProfile?>
    fun observeUsers(): Flow<List<UserProfile>>
    suspend fun updateProfile(profile: UserProfile)
    suspend fun setUserRole(uid: String, role: String)
    suspend fun recordAccess(uid: String)
}
