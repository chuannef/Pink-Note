@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.pinknote.app.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pinknote.app.domain.model.CycleSettings
import com.pinknote.app.domain.model.DailyLog
import com.pinknote.app.domain.repository.AuthRepository
import com.pinknote.app.domain.repository.CycleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class StatisticsUiState(
    val cycle: CycleSettings? = null,
    val logs: List<DailyLog> = emptyList(),
    val trackedMonths: Int = 0,
    val averagePain: Float = 0f
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    authRepository: AuthRepository,
    cycleRepository: CycleRepository
) : ViewModel() {
    val uiState: StateFlow<StatisticsUiState> = authRepository.currentUser.flatMapLatest { user ->
        if (user == null) {
            flowOf(StatisticsUiState())
        } else {
            combine(
                cycleRepository.observeCycle(user.uid),
                cycleRepository.observeDailyLogs(user.uid)
            ) { cycle, logs ->
                StatisticsUiState(
                    cycle = cycle,
                    logs = logs,
                    trackedMonths = logs.map { it.date.withDayOfMonth(1) }.distinct().size,
                    averagePain = logs.map { it.painLevel }.average().takeIf { !it.isNaN() }?.toFloat() ?: 0f
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatisticsUiState())
}
