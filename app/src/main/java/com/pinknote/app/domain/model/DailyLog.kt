package com.pinknote.app.domain.model

import java.time.LocalDate

data class DailyLog(
    val uid: String = "",
    val date: LocalDate = LocalDate.now(),
    val painLevel: Int = 0,
    val mood: String = "",
    val bodyTemperature: Float? = null,
    val weightKg: Float? = null,
    val isPeriodDay: Boolean? = null,
    val symptoms: List<String> = emptyList(),
    val discharge: String = "",
    val medicines: String = "",
    val hadSex: Boolean = false,
    val note: String = "",
    val updatedAtEpochMillis: Long = System.currentTimeMillis()
)
