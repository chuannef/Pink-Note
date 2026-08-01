package com.pinknote.app.presentation.localization

import com.pinknote.app.domain.model.AppLanguage
import com.pinknote.app.presentation.auth.passwordResetSentMessage
import com.pinknote.app.presentation.auth.validatePasswordResetEmail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppStringsTest {
    @Test
    fun `language labels come from localized strings`() {
        val vi = appStrings(AppLanguage.VI)
        val en = appStrings(AppLanguage.EN)

        assertEquals("Tiếng Việt", vi.languageLabel(AppLanguage.VI))
        assertEquals("English", vi.languageLabel(AppLanguage.EN))
        assertEquals("Vietnamese", en.languageLabel(AppLanguage.VI))
        assertEquals("English", en.languageLabel(AppLanguage.EN))
    }

    @Test
    fun `password reset messages follow selected language`() {
        val vi = appStrings(AppLanguage.VI)
        val en = appStrings(AppLanguage.EN)

        assertEquals("Hãy nhập email đã đăng ký.", validatePasswordResetEmail("", vi))
        assertEquals("Enter your registered email.", validatePasswordResetEmail("", en))
        assertTrue(passwordResetSentMessage(" user@example.com ", vi).contains("Nếu user@example.com"))
        assertTrue(passwordResetSentMessage(" user@example.com ", en).contains("If user@example.com"))
    }
}
