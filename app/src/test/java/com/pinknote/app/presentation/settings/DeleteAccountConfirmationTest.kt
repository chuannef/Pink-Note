package com.pinknote.app.presentation.settings

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteAccountConfirmationTest {
    @Test
    fun `requires exact delete confirmation text`() {
        assertFalse(isDeleteAccountConfirmationValid(""))
        assertFalse(isDeleteAccountConfirmationValid("xoa"))
        assertFalse(isDeleteAccountConfirmationValid("delete"))
        assertTrue(isDeleteAccountConfirmationValid(" XOA "))
    }
}
