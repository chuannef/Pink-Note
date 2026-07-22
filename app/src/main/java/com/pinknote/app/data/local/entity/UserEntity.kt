package com.pinknote.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val name: String,
    val email: String,
    val birthday: String?,
    val avatarUrl: String?,
    val heightCm: Float?,
    val weightKg: Float?,
    val healthGoal: String,
    val averageCycleLength: Int,
    val periodLength: Int,
    val createdAtEpochMillis: Long
)
