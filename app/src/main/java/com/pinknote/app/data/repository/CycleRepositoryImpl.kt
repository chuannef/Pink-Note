package com.pinknote.app.data.repository

import com.pinknote.app.data.local.dao.CycleDao
import com.pinknote.app.data.local.dao.DailyLogDao
import com.pinknote.app.data.remote.firebase.FirebaseDataSource
import com.pinknote.app.domain.model.CycleSettings
import com.pinknote.app.domain.model.DailyLog
import com.pinknote.app.domain.repository.CycleRepository
import com.pinknote.app.utils.DateUtils.toStorageString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CycleRepositoryImpl @Inject constructor(
    private val cycleDao: CycleDao,
    private val dailyLogDao: DailyLogDao,
    private val firebaseDataSource: FirebaseDataSource
) : CycleRepository {
    override fun observeCycle(uid: String): Flow<CycleSettings?> {
        return cycleDao.observeByUid(uid).map { it?.toDomain() }
    }

    override fun observeDailyLogs(uid: String): Flow<List<DailyLog>> {
        return dailyLogDao.observeByUid(uid).map { logs -> logs.map { it.toDomain() } }
    }

    override fun observeDailyLog(uid: String, date: LocalDate): Flow<DailyLog?> {
        return dailyLogDao.observeByDate(uid, date.toStorageString()).map { it?.toDomain() }
    }

    override suspend fun saveCycle(settings: CycleSettings) {
        cycleDao.upsert(settings.toEntity())
        runCatching { firebaseDataSource.saveCycle(settings) }
    }

    override suspend fun saveDailyLog(log: DailyLog) {
        dailyLogDao.upsert(log.toEntity())
        runCatching { firebaseDataSource.saveDailyLog(log) }
    }
}
