package com.pinknote.app.domain.model

import java.time.LocalDate

enum class CalendarDayType {
    PERIOD,
    OVULATION,
    FERTILE,
    NORMAL
}

data class CyclePrediction(
    val nextPeriodStart: LocalDate,
    val nextPeriodEnd: LocalDate,
    val ovulationDate: LocalDate,
    val fertileStart: LocalDate,
    val fertileEnd: LocalDate,
    val todayType: CalendarDayType,
    val cycleDay: Int,
    val countdownText: String
)

data class CalendarDay(
    val date: LocalDate,
    val type: CalendarDayType,
    val hasLog: Boolean = false
)
