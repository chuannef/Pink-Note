package com.pinknote.app.presentation.auth

import com.pinknote.app.presentation.localization.AppStrings

internal fun validatePasswordResetEmail(email: String, strings: AppStrings): String? {
    val normalizedEmail = email.trim()
    return when {
        normalizedEmail.isBlank() -> strings.registeredEmailRequired
        !normalizedEmail.contains("@") -> strings.invalidEmail
        else -> null
    }
}

internal fun passwordResetSentMessage(email: String, strings: AppStrings): String {
    return strings.passwordResetSentMessage.format(email.trim())
}
