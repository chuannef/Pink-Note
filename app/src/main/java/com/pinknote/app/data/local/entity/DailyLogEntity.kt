package com.pinknote.app.data.local.entity

import androidx.room.Entity

@Entity(tableName = "daily_logs", primaryKeys = ["uid", "date"])
data class DailyLogEntity(
    val uid: String,
    val date: String,
    val painLevel: Int,
    val mood: String,
    val bodyTemperature: Float?,
    val weightKg: Float?,
    val symptoms: String,
    val discharge: String,
    val medicines: String,
    val hadSex: Boolean,
    val note: String,
    val updatedAtEpochMillis: Long
)
