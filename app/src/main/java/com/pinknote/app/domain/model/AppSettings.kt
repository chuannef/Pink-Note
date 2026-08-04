package com.pinknote.app.domain.model

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class AppLanguage {
    VI,
    EN
}

enum class AppMode {
    CYCLE_TRACKING,
    PREGNANCY
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.VI,
    val appMode: AppMode = AppMode.CYCLE_TRACKING,
    val notificationsEnabled: Boolean = true
)
