package com.pinknote.app.presentation.home

import org.junit.Assert.assertEquals
import org.junit.Test

class CycleDateInputFormatterTest {
    @Test
    fun `adds date separators while typing digits`() {
        assertEquals("2026-", formatCycleDateInput(rawInput = "2026", previousValue = "202"))
        assertEquals("2026-07-", formatCycleDateInput(rawInput = "202607", previousValue = "2026-0"))
        assertEquals("2026-07-25", formatCycleDateInput(rawInput = "20260725", previousValue = "2026-07-2"))
    }

    @Test
    fun `normalizes pasted date text`() {
        assertEquals("2026-07-25", formatCycleDateInput(rawInput = "2026/07/25", previousValue = ""))
        assertEquals("2026-07-25", formatCycleDateInput(rawInput = "2026-07-25", previousValue = ""))
    }

    @Test
    fun `allows deleting an automatically inserted separator`() {
        assertEquals("2026", formatCycleDateInput(rawInput = "2026", previousValue = "2026-"))
        assertEquals("2026-07", formatCycleDateInput(rawInput = "2026-07", previousValue = "2026-07-"))
    }

    @Test
    fun `keeps month within valid range`() {
        assertEquals("2026-12-", formatCycleDateInput(rawInput = "202613", previousValue = "2026-1"))
        assertEquals("2026-12-01", formatCycleDateInput(rawInput = "20261301", previousValue = "2026-13-0"))
        assertEquals("2026-01-", formatCycleDateInput(rawInput = "202600", previousValue = "2026-0"))
    }

    @Test
    fun `treats single month digit above one as a leading zero month`() {
        assertEquals("2026-07-", formatCycleDateInput(rawInput = "20267", previousValue = "2026-"))
    }
}
