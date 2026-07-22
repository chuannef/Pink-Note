package com.pinknote.app.domain.repository

import com.pinknote.app.domain.model.CycleSettings
import com.pinknote.app.domain.model.DailyLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface CycleRepository {
    fun observeCycle(uid: String): Flow<CycleSettings?>
    fun observeDailyLogs(uid: String): Flow<List<DailyLog>>
    fun observeDailyLog(uid: String, date: LocalDate): Flow<DailyLog?>
    suspend fun saveCycle(settings: CycleSettings)
    suspend fun saveDailyLog(log: DailyLog)
}
