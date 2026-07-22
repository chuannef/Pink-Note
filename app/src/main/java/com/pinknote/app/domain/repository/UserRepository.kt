package com.pinknote.app.domain.repository

import com.pinknote.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeProfile(uid: String): Flow<UserProfile?>
    suspend fun updateProfile(profile: UserProfile)
}
