@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.pinknote.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pinknote.app.domain.model.CyclePrediction
import com.pinknote.app.domain.model.CycleSettings
import com.pinknote.app.domain.model.UserProfile
import com.pinknote.app.domain.repository.AuthRepository
import com.pinknote.app.domain.repository.CycleRepository
import com.pinknote.app.domain.usecase.PredictCycleUseCase
import com.pinknote.app.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val user: UserProfile? = null,
    val cycleSettings: CycleSettings = CycleSettings(
        lastPeriodStart = LocalDate.now(),
        cycleLength = Constants.DEFAULT_CYCLE_LENGTH,
        periodLength = Constants.DEFAULT_PERIOD_LENGTH
    ),
    val prediction: CyclePrediction? = null,
    val isEmpty: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val cycleRepository: CycleRepository,
    private val predictCycleUseCase: PredictCycleUseCase
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = authRepository.currentUser.flatMapLatest { user ->
        if (user == null) {
            flowOf(HomeUiState(isEmpty = true))
        } else {
            cycleRepository.observeCycle(user.uid).combine(flowOf(user)) { cycle, currentUser ->
                val settings = cycle ?: CycleSettings(
                    uid = currentUser.uid,
                    lastPeriodStart = LocalDate.now(),
                    cycleLength = Constants.DEFAULT_CYCLE_LENGTH,
                    periodLength = Constants.DEFAULT_PERIOD_LENGTH
                )
                HomeUiState(
                    user = currentUser,
                    cycleSettings = settings,
                    prediction = predictCycleUseCase(settings),
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
}
