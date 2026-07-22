package com.pinknote.app.data.repository

import com.pinknote.app.data.local.dao.UserDao
import com.pinknote.app.data.remote.firebase.FirebaseDataSource
import com.pinknote.app.domain.model.AppResult
import com.pinknote.app.domain.model.UserProfile
import com.pinknote.app.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firebaseDataSource: FirebaseDataSource,
    private val userDao: UserDao
) : AuthRepository {
    override val currentUser: Flow<UserProfile?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            trySend(user?.let {
                UserProfile(
                    uid = it.uid,
                    name = it.displayName.orEmpty(),
                    email = it.email.orEmpty(),
                    avatarUrl = it.photoUrl?.toString()
                )
            })
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun registerWithEmail(name: String, email: String, password: String): AppResult<UserProfile> {
        return runCatching {
            firebaseDataSource.registerWithEmail(name, email, password).also { userDao.upsert(it.toEntity()) }
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Error(it.message ?: "Không thể đăng ký", it) }
        )
    }

    override suspend fun loginWithEmail(email: String, password: String): AppResult<UserProfile> {
        return runCatching {
            firebaseDataSource.loginWithEmail(email, password).also { userDao.upsert(it.toEntity()) }
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Error(it.message ?: "Không thể đăng nhập", it) }
        )
    }

    override suspend fun loginWithGoogle(idToken: String): AppResult<UserProfile> {
        return runCatching {
            firebaseDataSource.loginWithGoogle(idToken).also { userDao.upsert(it.toEntity()) }
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Error(it.message ?: "Không thể đăng nhập Google", it) }
        )
    }

    override suspend fun sendPasswordReset(email: String): AppResult<Unit> {
        return runCatching { firebaseDataSource.sendPasswordReset(email) }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { AppResult.Error(it.message ?: "Không thể gửi email đặt lại mật khẩu", it) }
        )
    }

    override suspend fun logout() {
        firebaseDataSource.logout()
    }

    override suspend fun deleteAccount(): AppResult<Unit> {
        return runCatching { firebaseDataSource.deleteAccount() }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { AppResult.Error(it.message ?: "Không thể xóa tài khoản", it) }
        )
    }
}
