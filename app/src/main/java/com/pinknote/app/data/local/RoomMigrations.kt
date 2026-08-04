package com.pinknote.app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigrations {
    const val CURRENT_VERSION = 5

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE `users_migration_1_2` (
                    `uid` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `email` TEXT NOT NULL,
                    `birthday` TEXT,
                    `avatarUrl` TEXT,
                    `heightCm` REAL,
                    `weightKg` REAL,
                    `healthGoal` TEXT NOT NULL,
                    `averageCycleLength` INTEGER NOT NULL,
                    `periodLength` INTEGER NOT NULL,
                    `role` TEXT NOT NULL,
                    `createdAtEpochMillis` INTEGER NOT NULL,
                    PRIMARY KEY(`uid`)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO `users_migration_1_2` (
                    `uid`,
                    `name`,
                    `email`,
                    `birthday`,
                    `avatarUrl`,
                    `heightCm`,
                    `weightKg`,
                    `healthGoal`,
                    `averageCycleLength`,
                    `periodLength`,
                    `role`,
                    `createdAtEpochMillis`
                )
                SELECT
                    `uid`,
                    `name`,
                    `email`,
                    `birthday`,
                    `avatarUrl`,
                    `heightCm`,
                    `weightKg`,
                    `healthGoal`,
                    `averageCycleLength`,
                    `periodLength`,
                    'user',
                    `createdAtEpochMillis`
                FROM `users`
                """.trimIndent()
            )
            db.execSQL("DROP TABLE `users`")
            db.execSQL("ALTER TABLE `users_migration_1_2` RENAME TO `users`")
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE `users_migration_2_3` (
                    `uid` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `email` TEXT NOT NULL,
                    `birthday` TEXT,
                    `avatarUrl` TEXT,
                    `heightCm` REAL,
                    `weightKg` REAL,
                    `healthGoal` TEXT NOT NULL,
                    `averageCycleLength` INTEGER NOT NULL,
                    `periodLength` INTEGER NOT NULL,
                    `role` TEXT NOT NULL,
                    `accessCount` INTEGER NOT NULL,
                    `lastAccessAtEpochMillis` INTEGER,
                    `createdAtEpochMillis` INTEGER NOT NULL,
                    PRIMARY KEY(`uid`)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO `users_migration_2_3` (
                    `uid`,
                    `name`,
                    `email`,
                    `birthday`,
                    `avatarUrl`,
                    `heightCm`,
                    `weightKg`,
                    `healthGoal`,
                    `averageCycleLength`,
                    `periodLength`,
                    `role`,
                    `accessCount`,
                    `lastAccessAtEpochMillis`,
                    `createdAtEpochMillis`
                )
                SELECT
                    `uid`,
                    `name`,
                    `email`,
                    `birthday`,
                    `avatarUrl`,
                    `heightCm`,
                    `weightKg`,
                    `healthGoal`,
                    `averageCycleLength`,
                    `periodLength`,
                    `role`,
                    0,
                    NULL,
                    `createdAtEpochMillis`
                FROM `users`
                """.trimIndent()
            )
            db.execSQL("DROP TABLE `users`")
            db.execSQL("ALTER TABLE `users_migration_2_3` RENAME TO `users`")
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE `daily_logs_migration_3_4` (
                    `uid` TEXT NOT NULL,
                    `date` TEXT NOT NULL,
                    `painLevel` INTEGER NOT NULL,
                    `mood` TEXT NOT NULL,
                    `bodyTemperature` REAL,
                    `weightKg` REAL,
                    `isPeriodDay` INTEGER,
                    `symptoms` TEXT NOT NULL,
                    `discharge` TEXT NOT NULL,
                    `medicines` TEXT NOT NULL,
                    `hadSex` INTEGER NOT NULL,
                    `note` TEXT NOT NULL,
                    `updatedAtEpochMillis` INTEGER NOT NULL,
                    PRIMARY KEY(`uid`, `date`)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO `daily_logs_migration_3_4` (
                    `uid`,
                    `date`,
                    `painLevel`,
                    `mood`,
                    `bodyTemperature`,
                    `weightKg`,
                    `isPeriodDay`,
                    `symptoms`,
                    `discharge`,
                    `medicines`,
                    `hadSex`,
                    `note`,
                    `updatedAtEpochMillis`
                )
                SELECT
                    `uid`,
                    `date`,
                    `painLevel`,
                    `mood`,
                    `bodyTemperature`,
                    `weightKg`,
                    NULL,
                    `symptoms`,
                    `discharge`,
                    `medicines`,
                    `hadSex`,
                    `note`,
                    `updatedAtEpochMillis`
                FROM `daily_logs`
                """.trimIndent()
            )
            db.execSQL("DROP TABLE `daily_logs`")
            db.execSQL("ALTER TABLE `daily_logs_migration_3_4` RENAME TO `daily_logs`")
        }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `pregnancy` (
                    `uid` TEXT NOT NULL,
                    `lastMenstrualPeriod` TEXT,
                    `dueDate` TEXT,
                    `updatedAtEpochMillis` INTEGER NOT NULL,
                    PRIMARY KEY(`uid`)
                )
                """.trimIndent()
            )
        }
    }

    val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
}
