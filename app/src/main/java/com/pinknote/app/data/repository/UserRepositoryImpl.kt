package com.pinknote.app.data.repository

import com.pinknote.app.data.local.dao.UserDao
import com.pinknote.app.data.remote.firebase.FirebaseDataSource
import com.pinknote.app.domain.model.UserProfile
import com.pinknote.app.domain.repository.UserRepository
import com.pinknote.app.utils.AdminPolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val firebaseDataSource: FirebaseDataSource
) : UserRepository {
    override fun observeProfile(uid: String): Flow<UserProfile?> {
        return userDao.observeByUid(uid).map { it?.toDomain() }
    }

    override fun observeUsers(): Flow<List<UserProfile>> {
        return firebaseDataSource.observeUsers()
            .onEach { users -> users.forEach { userDao.upsert(it.toEntity()) } }
            .catch { emitAll(userDao.observeAll().map { users -> users.map { it.toDomain() } }) }
    }

    override suspend fun updateProfile(profile: UserProfile) {
        userDao.upsert(profile.toEntity())
        runCatching { firebaseDataSource.saveUser(profile) }
    }

    override suspend fun setUserRole(uid: String, role: String) {
        firebaseDataSource.setUserRole(uid, role)
        userDao.getByUid(uid)?.let { cached ->
            userDao.upsert(cached.copy(role = AdminPolicy.normalizeRole(role)))
        }
    }

    override suspend fun recordAccess(uid: String) {
        runCatching { firebaseDataSource.recordUserAccess(uid) }
    }
}
