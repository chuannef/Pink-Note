package com.pinknote.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pinknote.app.domain.model.AppLanguage
import com.pinknote.app.domain.model.AppSettings
import com.pinknote.app.domain.model.ThemeMode
import com.pinknote.app.domain.repository.AuthRepository
import com.pinknote.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    val currentEmail: StateFlow<String> = authRepository.currentUser
        .map { it?.email.orEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    fun setTheme(themeMode: ThemeMode) {
        save(settings.value.copy(themeMode = themeMode))
    }

    fun setLanguage(language: AppLanguage) {
        save(settings.value.copy(language = language))
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        save(settings.value.copy(notificationsEnabled = enabled))
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }

    fun sendChangePasswordEmail() {
        val email = currentEmail.value
        if (email.isNotBlank()) {
            viewModelScope.launch { authRepository.sendPasswordReset(email) }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch { authRepository.deleteAccount() }
    }

    private fun save(appSettings: AppSettings) {
        viewModelScope.launch {
            settingsRepository.saveSettings(appSettings)
        }
    }
}
