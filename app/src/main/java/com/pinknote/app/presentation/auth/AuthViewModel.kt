package com.pinknote.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pinknote.app.domain.model.AppResult
import com.pinknote.app.domain.repository.AuthRepository
import com.pinknote.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    private val accessRecordedUids = mutableSetOf<String>()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.value = _uiState.value.copy(isAuthenticated = user != null)
                if (user != null && accessRecordedUids.add(user.uid)) {
                    launch { userRepository.recordAccess(user.uid) }
                } else if (user == null) {
                    accessRecordedUids.clear()
                }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.loginWithEmail(email.trim(), password)) {
                is AppResult.Success -> _uiState.value = AuthUiState(isAuthenticated = true)
                is AppResult.Error -> _uiState.value = AuthUiState(message = result.message)
                AppResult.Loading -> Unit
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.registerWithEmail(name.trim(), email.trim(), password)) {
                is AppResult.Success -> _uiState.value = AuthUiState(isAuthenticated = true)
                is AppResult.Error -> _uiState.value = AuthUiState(message = result.message)
                AppResult.Loading -> Unit
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.loginWithGoogle(idToken)) {
                is AppResult.Success -> _uiState.value = AuthUiState(isAuthenticated = true)
                is AppResult.Error -> _uiState.value = AuthUiState(message = result.message)
                AppResult.Loading -> Unit
            }
        }
    }

    fun showAuthError(message: String) {
        _uiState.value = _uiState.value.copy(isLoading = false, message = message)
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = authRepository.sendPasswordReset(email.trim())) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(isLoading = false, message = "Đã gửi email đặt lại mật khẩu")
                is AppResult.Error -> _uiState.value = _uiState.value.copy(isLoading = false, message = result.message)
                AppResult.Loading -> Unit
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState(isAuthenticated = false)
        }
    }
}
