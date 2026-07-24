package com.pinknote.app.domain.model

import java.time.LocalDate

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val birthday: LocalDate? = null,
    val avatarUrl: String? = null,
    val heightCm: Float? = null,
    val weightKg: Float? = null,
    val healthGoal: String = "",
    val averageCycleLength: Int = 28,
    val periodLength: Int = 5,
    val role: String = "user",
    val accessCount: Long = 0L,
    val lastAccessAtEpochMillis: Long? = null,
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)
