@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.pinknote.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pinknote.app.domain.model.AppMode
import com.pinknote.app.domain.model.CyclePrediction
import com.pinknote.app.domain.model.CycleSettings
import com.pinknote.app.domain.model.PregnancySettings
import com.pinknote.app.domain.model.PregnancySummary
import com.pinknote.app.domain.model.UserProfile
import com.pinknote.app.domain.repository.AuthRepository
import com.pinknote.app.domain.repository.CycleRepository
import com.pinknote.app.domain.repository.PregnancyRepository
import com.pinknote.app.domain.repository.SettingsRepository
import com.pinknote.app.domain.usecase.PredictCycleUseCase
import com.pinknote.app.domain.usecase.PredictPregnancyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val user: UserProfile? = null,
    val appMode: AppMode = AppMode.CYCLE_TRACKING,
    val cycleSettings: CycleSettings? = null,
    val prediction: CyclePrediction? = null,
    val pregnancySettings: PregnancySettings? = null,
    val pregnancySummary: PregnancySummary? = null,
    val isEmpty: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val cycleRepository: CycleRepository,
    private val pregnancyRepository: PregnancyRepository,
    private val settingsRepository: SettingsRepository,
    private val predictCycleUseCase: PredictCycleUseCase,
    private val predictPregnancyUseCase: PredictPregnancyUseCase
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = authRepository.currentUser.flatMapLatest { user ->
        if (user == null) {
            flowOf(HomeUiState(isEmpty = true))
        } else {
            combine(
                cycleRepository.observeCycle(user.uid),
                cycleRepository.observeDailyLogs(user.uid),
                pregnancyRepository.observePregnancy(user.uid),
                settingsRepository.settings,
                flowOf(user)
            ) { cycle, logs, pregnancy, settings, currentUser ->
                HomeUiState(
                    user = currentUser,
                    appMode = settings.appMode,
                    cycleSettings = cycle,
                    prediction = cycle?.let { settings -> predictCycleUseCase(settings, logs = logs) },
                    pregnancySettings = pregnancy,
                    pregnancySummary = pregnancy?.let(predictPregnancyUseCase::invoke),
                    isEmpty = cycle == null
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun saveCycle(lastPeriodStart: LocalDate, cycleLength: Int, periodLength: Int) {
        val uid = uiState.value.user?.uid ?: return
        viewModelScope.launch {
            cycleRepository.saveCycle(
                CycleSettings(
                    uid = uid,
                    lastPeriodStart = lastPeriodStart,
                    cycleLength = cycleLength,
                    periodLength = periodLength
                )
            )
        }
    }

    fun saveAppMode(appMode: AppMode) {
        viewModelScope.launch {
            val currentSettings = settingsRepository.settings.first()
            settingsRepository.saveSettings(currentSettings.copy(appMode = appMode))
        }
    }

    fun savePregnancy(lastMenstrualPeriod: LocalDate?, dueDate: LocalDate?) {
        val uid = uiState.value.user?.uid ?: return
        if (lastMenstrualPeriod == null && dueDate == null) return
        viewModelScope.launch {
            pregnancyRepository.savePregnancy(
                PregnancySettings(
                    uid = uid,
                    lastMenstrualPeriod = lastMenstrualPeriod,
                    dueDate = dueDate
                )
            )
        }
    }
}
