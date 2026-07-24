package com.pinknote.app.domain.usecase

import com.pinknote.app.domain.model.CalendarDayType
import com.pinknote.app.domain.model.CycleSettings
import com.pinknote.app.domain.model.PredictionConfidence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PredictCycleUseCaseTest {
    private val useCase = PredictCycleUseCase()

    @Test
    fun `today inside period returns correct period day`() {
        val prediction = useCase(
            settings = CycleSettings(
                lastPeriodStart = LocalDate.of(2026, 7, 1),
                cycleLength = 28,
                periodLength = 5
            ),
            today = LocalDate.of(2026, 7, 3)
        )

        assertEquals(CalendarDayType.PERIOD, prediction.todayType)
        assertEquals("Hôm nay là ngày thứ 3 của kỳ kinh", prediction.countdownText)
        assertEquals(LocalDate.of(2026, 7, 29), prediction.nextPeriodStart)
        assertEquals(PredictionConfidence.VERY_LOW, prediction.confidence)
    }

    @Test
    fun `ovulation date is fourteen days before next period`() {
        val prediction = useCase(
            settings = CycleSettings(
                lastPeriodStart = LocalDate.of(2026, 7, 1),
                cycleLength = 28,
                periodLength = 5
            ),
            today = LocalDate.of(2026, 7, 15)
        )

        assertEquals(CalendarDayType.OVULATION, prediction.todayType)
        assertEquals(LocalDate.of(2026, 7, 15), prediction.ovulationDate)
    }

    @Test
    fun `late period does not automatically create a new cycle`() {
        val prediction = useCase(
            settings = CycleSettings(
                lastPeriodStart = LocalDate.of(2026, 7, 1),
                cycleLength = 28,
                periodLength = 5
            ),
            today = LocalDate.of(2026, 7, 31)
        )

        assertEquals(LocalDate.of(2026, 7, 29), prediction.nextPeriodStart)
        assertEquals(CalendarDayType.LATE_PERIOD, prediction.todayType)
        assertEquals(2, prediction.lateDays)
        assertTrue(prediction.isLate)
    }

    @Test
    fun `calendar uses confirmed period days before predictions`() {
        val days = useCase.buildCalendarDays(
            settings = CycleSettings(
                lastPeriodStart = LocalDate.of(2026, 7, 1),
                cycleLength = 28,
                periodLength = 5
            ),
            monthStart = LocalDate.of(2026, 7, 1),
            loggedDates = setOf(LocalDate.of(2026, 7, 2), LocalDate.of(2026, 7, 15)),
            periodConfirmations = mapOf(
                LocalDate.of(2026, 7, 2) to false,
                LocalDate.of(2026, 7, 15) to true
            )
        )

        assertEquals(CalendarDayType.NORMAL, days.first { it.date == LocalDate.of(2026, 7, 2) }.type)
        assertEquals(CalendarDayType.PERIOD, days.first { it.date == LocalDate.of(2026, 7, 15) }.type)
    }

    @Test
    fun `calendar predicts multiple future cycles`() {
        val days = useCase.buildCalendarDays(
            settings = CycleSettings(
                lastPeriodStart = LocalDate.of(2026, 7, 1),
                cycleLength = 28,
                periodLength = 5
            ),
            monthStart = LocalDate.of(2026, 9, 1),
            loggedDates = emptySet(),
            today = LocalDate.of(2026, 7, 10)
        )

        assertEquals(CalendarDayType.OVULATION, days.first { it.date == LocalDate.of(2026, 9, 9) }.type)
        assertEquals(CalendarDayType.PREDICTED_PERIOD, days.first { it.date == LocalDate.of(2026, 9, 23) }.type)
    }
}
