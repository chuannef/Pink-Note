package com.pinknote.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pregnancy")
data class PregnancyEntity(
    @PrimaryKey val uid: String,
    val lastMenstrualPeriod: String?,
    val dueDate: String?,
    val updatedAtEpochMillis: Long
)
