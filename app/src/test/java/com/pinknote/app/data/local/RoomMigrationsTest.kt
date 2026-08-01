package com.pinknote.app.data.local

import org.junit.Assert.assertEquals
import org.junit.Test

class RoomMigrationsTest {
    @Test
    fun `all migrations cover every historical database version`() {
        assertEquals(4, RoomMigrations.CURRENT_VERSION)
        assertEquals(
            listOf(1 to 2, 2 to 3, 3 to 4),
            RoomMigrations.ALL_MIGRATIONS.map { it.startVersion to it.endVersion }
        )
    }
}
