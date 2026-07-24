package com.pinknote.app.data.repository

import com.pinknote.app.data.local.entity.CycleEntity
import com.pinknote.app.data.local.entity.DailyLogEntity
import com.pinknote.app.data.local.entity.ReminderEntity
import com.pinknote.app.data.local.entity.UserEntity
import com.pinknote.app.domain.model.CycleSettings
import com.pinknote.app.domain.model.DailyLog
import com.pinknote.app.domain.model.Reminder
import com.pinknote.app.domain.model.ReminderType
import com.pinknote.app.domain.model.UserProfile
import com.pinknote.app.utils.DateUtils.parseStorageDate
import com.pinknote.app.utils.DateUtils.toStorageString
import java.time.LocalDateTime

fun UserProfile.toEntity() = UserEntity(
    uid = uid,
    name = name,
    email = email,
    birthday = birthday?.toStorageString(),
    avatarUrl = avatarUrl,
    heightCm = heightCm,
    weightKg = weightKg,
    healthGoal = healthGoal,
    averageCycleLength = averageCycleLength,
    periodLength = periodLength,
    role = role,
    createdAtEpochMillis = createdAtEpochMillis
)

fun UserEntity.toDomain() = UserProfile(
    uid = uid,
    name = name,
    email = email,
    birthday = birthday?.let(::parseStorageDate),
    avatarUrl = avatarUrl,
    heightCm = heightCm,
    weightKg = weightKg,
    healthGoal = healthGoal,
    averageCycleLength = averageCycleLength,
    periodLength = periodLength,
    role = role,
    createdAtEpochMillis = createdAtEpochMillis
)

fun CycleSettings.toEntity() = CycleEntity(
    uid = uid,
    lastPeriodStart = lastPeriodStart.toStorageString(),
    cycleLength = cycleLength,
    periodLength = periodLength,
    updatedAtEpochMillis = updatedAtEpochMillis
)

fun CycleEntity.toDomain() = CycleSettings(
    uid = uid,
    lastPeriodStart = parseStorageDate(lastPeriodStart),
    cycleLength = cycleLength,
    periodLength = periodLength,
    updatedAtEpochMillis = updatedAtEpochMillis
)

fun DailyLog.toEntity() = DailyLogEntity(
    uid = uid,
    date = date.toStorageString(),
    painLevel = painLevel,
    mood = mood,
    bodyTemperature = bodyTemperature,
    weightKg = weightKg,
    symptoms = symptoms.joinToString("|"),
    discharge = discharge,
    medicines = medicines,
    hadSex = hadSex,
    note = note,
    updatedAtEpochMillis = updatedAtEpochMillis
)

fun DailyLogEntity.toDomain() = DailyLog(
    uid = uid,
    date = parseStorageDate(date),
    painLevel = painLevel,
    mood = mood,
    bodyTemperature = bodyTemperature,
    weightKg = weightKg,
    symptoms = symptoms.split("|").filter { it.isNotBlank() },
    discharge = discharge,
    medicines = medicines,
    hadSex = hadSex,
    note = note,
    updatedAtEpochMillis = updatedAtEpochMillis
)

fun Reminder.toEntity() = ReminderEntity(
    id = id,
    uid = uid,
    type = type.name,
    title = title,
    message = message,
    scheduledAt = scheduledAt.toString(),
    enabled = enabled
)

fun ReminderEntity.toDomain() = Reminder(
    id = id,
    uid = uid,
    type = ReminderType.valueOf(type),
    title = title,
    message = message,
    scheduledAt = LocalDateTime.parse(scheduledAt),
    enabled = enabled
)
