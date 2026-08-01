package com.pinknote.app.presentation.auth

import com.pinknote.app.domain.model.AppLanguage
import com.pinknote.app.presentation.localization.appStrings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordResetInputTest {
    private val strings = appStrings(AppLanguage.VI)

    @Test
    fun `requires registered email`() {
        assertEquals("Hãy nhập email đã đăng ký.", validatePasswordResetEmail("", strings))
        assertEquals("Hãy nhập email đã đăng ký.", validatePasswordResetEmail("   ", strings))
    }

    @Test
    fun `rejects malformed email`() {
        assertEquals("Email chưa đúng định dạng.", validatePasswordResetEmail("pinknote", strings))
    }

    @Test
    fun `accepts normalized email`() {
        assertNull(validatePasswordResetEmail(" user@example.com ", strings))
    }

    @Test
    fun `success message does not reveal account existence`() {
        val message = passwordResetSentMessage(" user@example.com ", strings)

        assertTrue(message.contains("user@example.com"))
        assertTrue(message.contains("Nếu"))
    }
}
