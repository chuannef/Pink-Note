package com.pinknote.app.domain.model

import java.time.LocalDate

data class CycleSettings(
    val uid: String = "",
    val lastPeriodStart: LocalDate = LocalDate.now(),
    val cycleLength: Int = 28,
    val periodLength: Int = 5,
    val updatedAtEpochMillis: Long = System.currentTimeMillis()
)
