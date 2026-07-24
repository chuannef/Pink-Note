package com.pinknote.app.utils

object Constants {
    const val USERS_COLLECTION = "users"
    const val CYCLE_COLLECTION = "cycle"
    const val DAILY_LOGS_COLLECTION = "daily_logs"
    const val NOTIFICATIONS_COLLECTION = "notifications"
    const val SETTINGS_COLLECTION = "settings"

    const val DEFAULT_CYCLE_LENGTH = 28
    const val DEFAULT_PERIOD_LENGTH = 5
    const val FERTILE_WINDOW_START_OFFSET = 5
    const val FERTILE_WINDOW_END_OFFSET = 1
    const val REMINDER_DAYS_BEFORE_PERIOD = 3L

    const val SETTINGS_DATASTORE = "pinknote_settings"
    const val DATABASE_NAME = "pinknote.db"
    const val DATE_PATTERN = "yyyy-MM-dd"

    const val APP_VERSION = "1.0.0"
    const val DEVELOPER_NAME = "PinkNote"
    const val SUPPORT_EMAIL = "support@pinknote.app"
    const val PRIVACY_POLICY = "Privacy policy: local app data and Firebase data are used for account, cycle, reminder, and personal health tracking features."
}
