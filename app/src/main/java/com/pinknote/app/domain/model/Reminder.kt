package com.pinknote.app.domain.model

import java.time.LocalDateTime

enum class ReminderType {
    BEFORE_PERIOD,
    PERIOD_START,
    OVULATION,
    MEDICINE,
    WATER,
    WORKOUT
}

data class Reminder(
    val id: String = "",
    val uid: String = "",
    val type: ReminderType = ReminderType.BEFORE_PERIOD,
    val title: String = "",
    val message: String = "",
    val scheduledAt: LocalDateTime = LocalDateTime.now(),
    val enabled: Boolean = true
)
