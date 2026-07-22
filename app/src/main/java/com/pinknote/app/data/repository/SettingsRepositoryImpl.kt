package com.pinknote.app.data.repository

import com.pinknote.app.data.local.SettingsDataStore
import com.pinknote.app.domain.model.AppSettings
import com.pinknote.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : SettingsRepository {
    override val settings: Flow<AppSettings> = settingsDataStore.settings

    override suspend fun saveSettings(settings: AppSettings) {
        settingsDataStore.save(settings)
    }
}
