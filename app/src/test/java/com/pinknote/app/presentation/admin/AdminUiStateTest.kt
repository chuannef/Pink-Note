package com.pinknote.app.presentation.admin

import com.pinknote.app.domain.model.UserProfile
import com.pinknote.app.utils.AdminPolicy
import org.junit.Assert.assertEquals
import org.junit.Test

class AdminUiStateTest {
    @Test
    fun `dashboard stats summarize users and access counts`() {
        val state = AdminUiState(
            users = listOf(
                UserProfile(
                    uid = "admin-1",
                    email = "admin@pinknote.app",
                    role = AdminPolicy.ROLE_ADMIN,
                    accessCount = 7,
                    lastAccessAtEpochMillis = 1_700_000_000_000
                ),
                UserProfile(
                    uid = "user-1",
                    email = "user@pinknote.app",
                    role = AdminPolicy.ROLE_USER,
                    accessCount = 3,
                    lastAccessAtEpochMillis = 1_700_000_100_000
                ),
                UserProfile(
                    uid = "user-2",
                    email = "quiet@pinknote.app",
                    role = AdminPolicy.ROLE_USER
                )
            )
        )

        assertEquals(1, state.adminCount)
        assertEquals(2, state.standardUserCount)
        assertEquals(10L, state.totalAccessCount)
        assertEquals(1, state.neverAccessedCount)
    }
}
