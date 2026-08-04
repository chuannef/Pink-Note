package com.pinknote.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pinknote.app.data.local.dao.CycleDao
import com.pinknote.app.data.local.dao.DailyLogDao
import com.pinknote.app.data.local.dao.PregnancyDao
import com.pinknote.app.data.local.dao.ReminderDao
import com.pinknote.app.data.local.dao.UserDao
import com.pinknote.app.data.local.entity.CycleEntity
import com.pinknote.app.data.local.entity.DailyLogEntity
import com.pinknote.app.data.local.entity.PregnancyEntity
import com.pinknote.app.data.local.entity.ReminderEntity
import com.pinknote.app.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        CycleEntity::class,
        DailyLogEntity::class,
        PregnancyEntity::class,
        ReminderEntity::class
    ],
    version = RoomMigrations.CURRENT_VERSION,
    exportSchema = true
)
abstract class PinkNoteDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun cycleDao(): CycleDao
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun pregnancyDao(): PregnancyDao
    abstract fun reminderDao(): ReminderDao
}
