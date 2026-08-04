package com.pinknote.app.data.repository

import com.pinknote.app.data.local.dao.PregnancyDao
import com.pinknote.app.data.remote.firebase.FirebaseDataSource
import com.pinknote.app.domain.model.PregnancySettings
import com.pinknote.app.domain.repository.PregnancyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PregnancyRepositoryImpl @Inject constructor(
    private val pregnancyDao: PregnancyDao,
    private val firebaseDataSource: FirebaseDataSource
) : PregnancyRepository {
    override fun observePregnancy(uid: String): Flow<PregnancySettings?> {
        return pregnancyDao.observeByUid(uid).map { it?.toDomain() }
    }

    override suspend fun savePregnancy(settings: PregnancySettings) {
        pregnancyDao.upsert(settings.toEntity())
        runCatching { firebaseDataSource.savePregnancy(settings) }
    }
}
