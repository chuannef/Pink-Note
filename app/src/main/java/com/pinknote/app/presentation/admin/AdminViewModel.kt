@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.pinknote.app.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pinknote.app.domain.model.UserProfile
import com.pinknote.app.domain.repository.AuthRepository
import com.pinknote.app.domain.repository.UserRepository
import com.pinknote.app.utils.AdminPolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(
    val currentUser: UserProfile? = null,
    val users: List<UserProfile> = emptyList(),
    val errorMessage: String? = null
) {
    val isAllowed: Boolean = AdminPolicy.isAdmin(currentUser?.role.orEmpty())
    val adminCount: Int = users.count { AdminPolicy.isAdmin(it.role) }
    val standardUserCount: Int = users.size - adminCount
}

@HiltViewModel
class AdminViewModel @Inject constructor(
    authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val currentProfile = authRepository.currentUser.flatMapLatest { user ->
        user?.uid?.let { uid ->
            userRepository.observeProfile(uid).map { profile -> profile ?: user }
        } ?: flowOf(null)
    }

    val uiState: StateFlow<AdminUiState> = combine(
        currentProfile,
        userRepository.observeUsers()
    ) { currentUser, users ->
        AdminUiState(
            currentUser = currentUser,
            users = users.sortedWith(
                compareByDescending<UserProfile> { AdminPolicy.isAdmin(it.role) }
                    .thenByDescending { it.createdAtEpochMillis }
            )
        )
    }.catch { error ->
        emit(AdminUiState(errorMessage = error.message ?: "Không tải được dữ liệu quản trị."))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AdminUiState())

    fun setRole(uid: String, role: String) {
        if (!uiState.value.isAllowed) return

        viewModelScope.launch {
            runCatching { userRepository.setUserRole(uid, role) }
        }
    }
}
