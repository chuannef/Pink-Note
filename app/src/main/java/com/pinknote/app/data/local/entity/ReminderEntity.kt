package com.pinknote.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class ReminderEntity(
    @PrimaryKey val id: String,
    val uid: String,
    val type: String,
    val title: String,
    val message: String,
    val scheduledAt: String,
    val enabled: Boolean
)
