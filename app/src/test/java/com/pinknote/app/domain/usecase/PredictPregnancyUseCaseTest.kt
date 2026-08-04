package com.pinknote.app.domain.usecase

import com.pinknote.app.domain.model.PregnancySettings
import com.pinknote.app.domain.model.PregnancyTrimester
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PredictPregnancyUseCaseTest {
    private val useCase = PredictPregnancyUseCase()

    @Test
    fun `calculates pregnancy summary from last menstrual period`() {
        val summary = useCase(
            settings = PregnancySettings(lastMenstrualPeriod = LocalDate.of(2026, 7, 1)),
            today = LocalDate.of(2026, 8, 4)
        )

        assertNotNull(summary)
        requireNotNull(summary)
        assertEquals(5, summary.gestationalWeek)
        assertEquals(6, summary.gestationalDayOfWeek)
        assertEquals(PregnancyTrimester.FIRST, summary.trimester)
        assertEquals(LocalDate.of(2027, 4, 7), summary.dueDate)
        assertEquals(246, summary.daysUntilDue)
        assertTrue(summary.progress in 0f..1f)
    }

    @Test
    fun `calculates pregnancy summary from due date when lmp is missing`() {
        val summary = useCase(
            settings = PregnancySettings(dueDate = LocalDate.of(2027, 4, 7)),
            today = LocalDate.of(2026, 8, 4)
        )

        assertNotNull(summary)
        requireNotNull(summary)
        assertEquals(5, summary.gestationalWeek)
        assertEquals(LocalDate.of(2027, 4, 7), summary.dueDate)
    }

    @Test
    fun `returns null when pregnancy has not been set up`() {
        assertNull(useCase(PregnancySettings()))
    }
}
