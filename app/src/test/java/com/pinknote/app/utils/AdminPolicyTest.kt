package com.pinknote.app.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdminPolicyTest {
    @Test
    fun `only admin role grants admin access`() {
        assertTrue(AdminPolicy.isAdmin("admin"))
        assertTrue(AdminPolicy.isAdmin("ADMIN"))
        assertFalse(AdminPolicy.isAdmin("user"))
        assertFalse(AdminPolicy.isAdmin(""))
    }

    @Test
    fun `missing or unknown role is normalized to user`() {
        assertEquals("user", AdminPolicy.normalizeRole(null))
        assertEquals("user", AdminPolicy.normalizeRole(""))
        assertEquals("user", AdminPolicy.normalizeRole("owner"))
        assertEquals("admin", AdminPolicy.normalizeRole("ADMIN"))
    }
}
