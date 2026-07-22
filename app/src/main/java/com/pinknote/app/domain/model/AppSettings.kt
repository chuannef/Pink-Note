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

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.VI,
    val notificationsEnabled: Boolean = true
)
