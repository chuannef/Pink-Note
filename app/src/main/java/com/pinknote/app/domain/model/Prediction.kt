package com.pinknote.app.domain.model

import java.time.LocalDate

enum class CalendarDayType {
    PERIOD,
    PREDICTED_PERIOD,
    LATE_PERIOD,
    OVULATION,
    FERTILE,
    PMS,
    NORMAL
}

enum class PredictionConfidence {
    VERY_LOW,
    LOW,
    MEDIUM,
    HIGH
}

enum class FertilityLevel {
    VERY_LOW,
    LOW,
    MEDIUM,
    HIGH,
    VERY_HIGH,
    PEAK
}

data class FertilityEstimate(
    val date: LocalDate,
    val probabilityPercent: Int,
    val level: FertilityLevel
)

data class CyclePrediction(
    val nextPeriodStart: LocalDate,
    val nextPeriodEnd: LocalDate,
    val ovulationDate: LocalDate,
    val fertileStart: LocalDate,
    val fertileEnd: LocalDate,
    val todayType: CalendarDayType,
    val cycleDay: Int,
    val countdownText: String,
    val confidence: PredictionConfidence = PredictionConfidence.VERY_LOW,
    val weightedCycleLength: Int = 28,
    val weightedPeriodLength: Int = 5,
    val cycleVariabilityDays: Int = 0,
    val isLate: Boolean = false,
    val lateDays: Int = 0,
    val pmsStart: LocalDate = nextPeriodStart.minusDays(7),
    val pmsEnd: LocalDate = nextPeriodStart.minusDays(1),
    val cycleProgress: Float = 0f,
    val historyCycleCount: Int = 0,
    val fertilityTodayPercent: Int = 1,
    val fertilityTodayLevel: FertilityLevel = FertilityLevel.VERY_LOW,
    val fertilityEstimates: List<FertilityEstimate> = emptyList(),
    val uncertaintyText: String = "",
    val lateReasons: List<String> = emptyList(),
    val disclaimer: String = "Predictions are estimates for personal tracking only and should not be used as medical advice, contraception, or pregnancy planning."
)

data class CalendarDay(
    val date: LocalDate,
    val type: CalendarDayType,
    val hasLog: Boolean = false
)
