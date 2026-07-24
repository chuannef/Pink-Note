@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.pinknote.app.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pinknote.app.domain.model.UserProfile
import com.pinknote.app.domain.repository.AuthRepository
import com.pinknote.app.domain.repository.UserRepository
import com.pinknote.app.utils.AdminPolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class AdminUiState(
    val currentUser: UserProfile? = null,
    val users: List<UserProfile> = emptyList(),
    val actionMessage: String? = null,
    val errorMessage: String? = null
) {
    val isAllowed: Boolean = AdminPolicy.isAdmin(currentUser?.role.orEmpty())
    val adminCount: Int = users.count { AdminPolicy.isAdmin(it.role) }
    val standardUserCount: Int = users.size - adminCount
    val totalAccessCount: Long = users.sumOf { it.accessCount }
    val activeTodayCount: Int = users.count { it.lastAccessAtEpochMillis?.isToday() == true }
    val neverAccessedCount: Int = users.count { it.lastAccessAtEpochMillis == null }
}

@HiltViewModel
class AdminViewModel @Inject constructor(
    authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val actionMessage = MutableStateFlow<String?>(null)

    private val currentProfile = authRepository.currentUser.flatMapLatest { user ->
        user?.uid?.let { uid ->
            userRepository.observeProfile(uid).map { profile -> profile ?: user }
        } ?: flowOf(null)
    }

    val uiState: StateFlow<AdminUiState> = combine(
        currentProfile,
        userRepository.observeUsers(),
        actionMessage
    ) { currentUser, users, message ->
        AdminUiState(
            currentUser = currentUser,
            actionMessage = message,
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
                .onSuccess { actionMessage.value = "Đã cập nhật quyền người dùng." }
                .onFailure { actionMessage.value = "Không thể cập nhật quyền: ${it.message.orEmpty()}" }
        }
    }

    fun promoteAdminByEmail(email: String) {
        if (!uiState.value.isAllowed) return

        val normalizedEmail = email.trim()
        if (normalizedEmail.isBlank()) {
            actionMessage.value = "Hãy nhập email cần cấp quyền admin."
            return
        }

        val target = uiState.value.users.firstOrNull { it.email.equals(normalizedEmail, ignoreCase = true) }
        if (target == null) {
            actionMessage.value = "Không tìm thấy user với email $normalizedEmail."
            return
        }

        if (AdminPolicy.isAdmin(target.role)) {
            actionMessage.value = "${target.email} đã là admin."
            return
        }

        setRole(target.uid, AdminPolicy.ROLE_ADMIN)
    }

    fun clearActionMessage() {
        actionMessage.value = null
    }
}

private fun Long.isToday(): Boolean {
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate() == LocalDate.now()
}
