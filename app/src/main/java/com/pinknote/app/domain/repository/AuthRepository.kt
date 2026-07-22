package com.pinknote.app.domain.repository

import com.pinknote.app.domain.model.AppResult
import com.pinknote.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<UserProfile?>

    suspend fun registerWithEmail(name: String, email: String, password: String): AppResult<UserProfile>
    suspend fun loginWithEmail(email: String, password: String): AppResult<UserProfile>
    suspend fun loginWithGoogle(idToken: String): AppResult<UserProfile>
    suspend fun sendPasswordReset(email: String): AppResult<Unit>
    suspend fun logout()
    suspend fun deleteAccount(): AppResult<Unit>
}
