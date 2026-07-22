@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.pinknote.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pinknote.app.domain.model.UserProfile
import com.pinknote.app.domain.repository.AuthRepository
import com.pinknote.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    val profile: StateFlow<UserProfile?> = authRepository.currentUser.flatMapLatest { user ->
        user?.uid?.let { userRepository.observeProfile(it) } ?: flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun update(profile: UserProfile) {
        viewModelScope.launch {
            userRepository.updateProfile(profile)
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
