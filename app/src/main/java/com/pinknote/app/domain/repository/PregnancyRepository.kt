package com.pinknote.app.domain.repository

import com.pinknote.app.domain.model.PregnancySettings
import kotlinx.coroutines.flow.Flow

interface PregnancyRepository {
    fun observePregnancy(uid: String): Flow<PregnancySettings?>
    suspend fun savePregnancy(settings: PregnancySettings)
}
