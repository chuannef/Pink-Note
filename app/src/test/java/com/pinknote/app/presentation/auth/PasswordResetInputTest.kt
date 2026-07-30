package com.pinknote.app.presentation.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordResetInputTest {
    @Test
    fun `requires registered email`() {
        assertEquals("Hãy nhập email đã đăng ký.", validatePasswordResetEmail(""))
        assertEquals("Hãy nhập email đã đăng ký.", validatePasswordResetEmail("   "))
    }

    @Test
    fun `rejects malformed email`() {
        assertEquals("Email chưa đúng định dạng.", validatePasswordResetEmail("pinknote"))
    }

    @Test
    fun `accepts normalized email`() {
        assertNull(validatePasswordResetEmail(" user@example.com "))
    }

    @Test
    fun `success message does not reveal account existence`() {
        val message = passwordResetSentMessage(" user@example.com ")

        assertTrue(message.contains("user@example.com"))
        assertTrue(message.contains("Nếu"))
    }
}
