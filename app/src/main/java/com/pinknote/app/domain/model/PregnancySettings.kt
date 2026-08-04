package com.pinknote.app.domain.model

import java.time.LocalDate

data class PregnancySettings(
    val uid: String = "",
    val lastMenstrualPeriod: LocalDate? = null,
    val dueDate: LocalDate? = null,
    val updatedAtEpochMillis: Long = System.currentTimeMillis()
)

enum class PregnancyTrimester {
    FIRST,
    SECOND,
    THIRD
}

data class PregnancySummary(
    val gestationalWeek: Int,
    val gestationalDayOfWeek: Int,
    val gestationalDays: Int,
    val trimester: PregnancyTrimester,
    val dueDate: LocalDate,
    val daysUntilDue: Long,
    val progress: Float,
    val statusText: String
)
