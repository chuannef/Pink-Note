@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.pinknote.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pinknote.app.domain.model.AppLanguage
import com.pinknote.app.domain.model.AppMode
import com.pinknote.app.domain.model.AppSettings
import com.pinknote.app.domain.model.AppResult
import com.pinknote.app.domain.model.ThemeMode
import com.pinknote.app.domain.repository.AuthRepository
import com.pinknote.app.domain.repository.SettingsRepository
import com.pinknote.app.domain.repository.UserRepository
import com.pinknote.app.utils.AdminPolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
    userRepository: UserRepository
) : ViewModel() {
    private val _isLoggingOut = MutableStateFlow(false)
    val isLoggingOut: StateFlow<Boolean> = _isLoggingOut.asStateFlow()

    private val _logoutCompleted = MutableStateFlow(false)
    val logoutCompleted: StateFlow<Boolean> = _logoutCompleted.asStateFlow()

    private val _isDeletingAccount = MutableStateFlow(false)
    val isDeletingAccount: StateFlow<Boolean> = _isDeletingAccount.asStateFlow()

    private val _deleteAccountMessage = MutableStateFlow<String?>(null)
    val deleteAccountMessage: StateFlow<String?> = _deleteAccountMessage.asStateFlow()

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    val currentEmail: StateFlow<String> = authRepository.currentUser
        .map { it?.email.orEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val isAdmin: StateFlow<Boolean> = authRepository.currentUser
        .flatMapLatest { user ->
            user?.uid?.let { uid ->
                userRepository.observeProfile(uid).map { profile ->
                    AdminPolicy.isAdmin(profile?.role ?: user.role)
                }
            } ?: flowOf(false)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun setTheme(themeMode: ThemeMode) {
        save(settings.value.copy(themeMode = themeMode))
    }

    fun setLanguage(language: AppLanguage) {
        save(settings.value.copy(language = language))
    }

    fun setAppMode(appMode: AppMode) {
        save(settings.value.copy(appMode = appMode))
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        save(settings.value.copy(notificationsEnabled = enabled))
    }

    fun logout() {
        if (_isLoggingOut.value) return

        viewModelScope.launch {
            _isLoggingOut.value = true
            authRepository.logout()
            _isLoggingOut.value = false
            _logoutCompleted.value = true
        }
    }

    fun consumeLogoutNavigation() {
        _logoutCompleted.value = false
    }

    fun sendChangePasswordEmail() {
        val email = currentEmail.value
        if (email.isNotBlank()) {
            viewModelScope.launch { authRepository.sendPasswordReset(email) }
        }
    }

    fun deleteAccount() {
        if (_isDeletingAccount.value) return

        viewModelScope.launch {
            _isDeletingAccount.value = true
            _deleteAccountMessage.value = null
            when (val result = authRepository.deleteAccount()) {
                is AppResult.Success -> {
                    _isDeletingAccount.value = false
                    _logoutCompleted.value = true
                }
                is AppResult.Error -> {
                    _isDeletingAccount.value = false
                    _deleteAccountMessage.value = result.message
                }
                AppResult.Loading -> Unit
            }
        }
    }

    private fun save(appSettings: AppSettings) {
        viewModelScope.launch {
            settingsRepository.saveSettings(appSettings)
        }
    }
}
