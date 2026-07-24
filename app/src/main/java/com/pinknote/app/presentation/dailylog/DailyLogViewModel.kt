@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.pinknote.app.presentation.dailylog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pinknote.app.domain.model.DailyLog
import com.pinknote.app.domain.repository.AuthRepository
import com.pinknote.app.domain.repository.CycleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DailyLogViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val cycleRepository: CycleRepository
) : ViewModel() {
    private val selectedDate = MutableStateFlow(LocalDate.now())
    private var currentUid: String? = null
    private val _saveEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val saveEvents: SharedFlow<Unit> = _saveEvents.asSharedFlow()

    val log: StateFlow<DailyLog?> = authRepository.currentUser.flatMapLatest { user ->
        currentUid = user?.uid
        val uid = user?.uid ?: return@flatMapLatest flowOf(null)
        selectedDate.flatMapLatest { date -> cycleRepository.observeDailyLog(uid, date) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun saveLog(
        painLevel: Int,
        mood: String,
        bodyTemperature: Float?,
        weightKg: Float?,
        isPeriodDay: Boolean?,
        symptoms: List<String>,
        discharge: String,
        medicines: String,
        hadSex: Boolean,
        note: String
    ) {
        val uid = currentUid ?: return
        viewModelScope.launch {
            cycleRepository.saveDailyLog(
                DailyLog(
                    uid = uid,
                    date = selectedDate.value,
                    painLevel = painLevel,
                    mood = mood,
                    bodyTemperature = bodyTemperature,
                    weightKg = weightKg,
                    isPeriodDay = isPeriodDay,
                    symptoms = symptoms,
                    discharge = discharge,
                    medicines = medicines,
                    hadSex = hadSex,
                    note = note
                )
            )
            _saveEvents.emit(Unit)
        }
    }
}
