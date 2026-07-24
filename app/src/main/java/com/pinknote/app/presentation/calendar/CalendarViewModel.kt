@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.pinknote.app.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pinknote.app.domain.model.CalendarDay
import com.pinknote.app.domain.model.CalendarDayType
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
    val selectedDate: LocalDate = LocalDate.now(),
    val hasCycleSetup: Boolean = false
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
                val loggedDates = logs.map { it.date }.toSet()
                CalendarUiState(
                    month = currentMonth,
                    selectedDate = selected,
                    hasCycleSetup = cycle != null,
                    days = if (cycle == null) {
                        buildEmptyCalendarDays(currentMonth, loggedDates)
                    } else {
                        predictCycleUseCase.buildCalendarDays(
                            settings = cycle,
                            monthStart = currentMonth,
                            loggedDates = loggedDates,
                            periodConfirmations = logs.associate { it.date to it.isPeriodDay },
                            logs = logs
                        )
                    }
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

    private fun buildEmptyCalendarDays(monthStart: LocalDate, loggedDates: Set<LocalDate>): List<CalendarDay> {
        val firstDay = monthStart.withDayOfMonth(1)
        val endDay = firstDay.plusMonths(1).minusDays(1)
        return generateSequence(firstDay) { date ->
            if (date < endDay) date.plusDays(1) else null
        }.map { date ->
            CalendarDay(
                date = date,
                type = CalendarDayType.NORMAL,
                hasLog = loggedDates.contains(date)
            )
        }.toList()
    }
}
