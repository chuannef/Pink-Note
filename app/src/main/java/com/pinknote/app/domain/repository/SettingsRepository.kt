package com.pinknote.app.domain.repository

import com.pinknote.app.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun saveSettings(settings: AppSettings)
}
