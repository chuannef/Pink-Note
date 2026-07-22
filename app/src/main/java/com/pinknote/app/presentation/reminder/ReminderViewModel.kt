@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.pinknote.app.presentation.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pinknote.app.domain.model.Reminder
import com.pinknote.app.domain.model.ReminderType
import com.pinknote.app.domain.repository.AuthRepository
import com.pinknote.app.domain.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val reminderRepository: ReminderRepository
) : ViewModel() {
    private var currentUid: String? = null

    val reminders: StateFlow<List<Reminder>> = authRepository.currentUser.flatMapLatest { user ->
        currentUid = user?.uid
        user?.uid?.let { reminderRepository.observeReminders(it) } ?: flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun createReminder(type: ReminderType, title: String, message: String, scheduledAt: LocalDateTime) {
        val uid = currentUid ?: return
        viewModelScope.launch {
            reminderRepository.saveReminder(
                Reminder(
                    id = UUID.randomUUID().toString(),
                    uid = uid,
                    type = type,
                    title = title,
                    message = message,
                    scheduledAt = scheduledAt
                )
            )
        }
    }

    fun cancel(reminderId: String) {
        viewModelScope.launch {
            reminderRepository.cancelReminder(reminderId)
        }
    }
}
