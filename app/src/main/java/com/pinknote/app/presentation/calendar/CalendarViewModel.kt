@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.pinknote.app.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pinknote.app.domain.model.CalendarDay
import com.pinknote.app.domain.model.CycleSettings
import com.pinknote.app.domain.repository.AuthRepository
import com.pinknote.app.domain.repository.CycleRepository
import com.pinknote.app.domain.usecase.PredictCycleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

data class CalendarUiState(
    val month: LocalDate = LocalDate.now().withDayOfMonth(1),
    val days: List<CalendarDay> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now()
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val cycleRepository: CycleRepository,
    private val predictCycleUseCase: PredictCycleUseCase
) : ViewModel() {
    private val month = MutableStateFlow(LocalDate.now().withDayOfMonth(1))
    private val selectedDate = MutableStateFlow(LocalDate.now())

    val uiState: StateFlow<CalendarUiState> = authRepository.currentUser.flatMapLatest { user ->
        if (user == null) {
            flowOf(CalendarUiState())
        } else {
            combine(
                month,
                selectedDate,
                cycleRepository.observeCycle(user.uid),
                cycleRepository.observeDailyLogs(user.uid)
            ) { currentMonth, selected, cycle, logs ->
                val settings = cycle ?: CycleSettings(uid = user.uid)
                CalendarUiState(
                    month = currentMonth,
                    selectedDate = selected,
                    days = predictCycleUseCase.buildCalendarDays(
                        settings = settings,
                        monthStart = currentMonth,
                        loggedDates = logs.map { it.date }.toSet(),
                        periodConfirmations = logs.associate { it.date to it.isPeriodDay }
                    )
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarUiState())

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun nextMonth() {
        month.value = month.value.plusMonths(1)
    }

    fun previousMonth() {
        month.value = month.value.minusMonths(1)
    }
}
