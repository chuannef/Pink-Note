package com.pinknote.app.data.repository

import com.pinknote.app.data.local.dao.UserDao
import com.pinknote.app.data.remote.firebase.FirebaseDataSource
import com.pinknote.app.domain.model.UserProfile
import com.pinknote.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

    override suspend fun updateProfile(profile: UserProfile) {
        userDao.upsert(profile.toEntity())
        runCatching { firebaseDataSource.saveUser(profile) }
    }
}
