package com.pinknote.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pinknote.app.domain.model.AppLanguage
import com.pinknote.app.domain.model.AppSettings
import com.pinknote.app.domain.model.ThemeMode
import com.pinknote.app.utils.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(Constants.SETTINGS_DATASTORE)

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val themeMode = stringPreferencesKey("theme_mode")
        val language = stringPreferencesKey("language")
        val notificationsEnabled = booleanPreferencesKey("notifications_enabled")
    }

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { preferences ->
        AppSettings(
            themeMode = preferences[Keys.themeMode]?.let(ThemeMode::valueOf) ?: ThemeMode.SYSTEM,
            language = preferences[Keys.language]?.let(AppLanguage::valueOf) ?: AppLanguage.VI,
            notificationsEnabled = preferences[Keys.notificationsEnabled] ?: true
        )
    }

    suspend fun save(settings: AppSettings) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.themeMode] = settings.themeMode.name
            preferences[Keys.language] = settings.language.name
            preferences[Keys.notificationsEnabled] = settings.notificationsEnabled
        }
    }
}
