package com.pinknote.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cycle")
data class CycleEntity(
    @PrimaryKey val uid: String,
    val lastPeriodStart: String,
    val cycleLength: Int,
    val periodLength: Int,
    val updatedAtEpochMillis: Long
)
