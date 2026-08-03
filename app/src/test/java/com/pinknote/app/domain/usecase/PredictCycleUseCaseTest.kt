package com.pinknote.app.domain.usecase

import com.pinknote.app.domain.model.CalendarDayType
import com.pinknote.app.domain.model.CycleSettings
import com.pinknote.app.domain.model.DailyLog
import com.pinknote.app.domain.model.FertilityLevel
import com.pinknote.app.domain.model.PredictionConfidence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun `late period moves prediction after today without creating a confirmed cycle`() {
        val prediction = useCase(
            settings = CycleSettings(
                lastPeriodStart = LocalDate.of(2026, 7, 1),
                cycleLength = 28,
                periodLength = 5
            ),
            today = LocalDate.of(2026, 7, 31)
        )

        assertEquals(LocalDate.of(2026, 8, 1), prediction.nextPeriodStart)
        assertEquals(LocalDate.of(2026, 8, 5), prediction.nextPeriodEnd)
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

    @Test
    fun `late unconfirmed period keeps moving the full predicted period window after today`() {
        val settings = CycleSettings(
            lastPeriodStart = LocalDate.of(2026, 7, 1),
            cycleLength = 28,
            periodLength = 5
        )
        val noPeriodLogs = listOf(
            DailyLog(date = LocalDate.of(2026, 7, 28), isPeriodDay = false),
            DailyLog(date = LocalDate.of(2026, 7, 29), isPeriodDay = false),
            DailyLog(date = LocalDate.of(2026, 7, 30), isPeriodDay = false),
            DailyLog(date = LocalDate.of(2026, 7, 31), isPeriodDay = false)
        )

        val prediction = useCase(
            settings = settings,
            today = LocalDate.of(2026, 8, 2),
            logs = noPeriodLogs
        )
        val augustDays = useCase.buildCalendarDays(
            settings = settings,
            monthStart = LocalDate.of(2026, 8, 1),
            loggedDates = noPeriodLogs.map { it.date }.toSet(),
            periodConfirmations = noPeriodLogs.associate { it.date to it.isPeriodDay },
            today = LocalDate.of(2026, 8, 2),
            logs = noPeriodLogs
        )
        val julyDays = useCase.buildCalendarDays(
            settings = settings,
            monthStart = LocalDate.of(2026, 7, 1),
            loggedDates = noPeriodLogs.map { it.date }.toSet(),
            periodConfirmations = noPeriodLogs.associate { it.date to it.isPeriodDay },
            today = LocalDate.of(2026, 8, 2),
            logs = noPeriodLogs
        )

        assertEquals(LocalDate.of(2026, 8, 3), prediction.nextPeriodStart)
        assertEquals(LocalDate.of(2026, 8, 7), prediction.nextPeriodEnd)
        assertEquals(4, prediction.lateDays)
        assertTrue(prediction.isLate)
        assertFalse(julyDays.first { it.date == LocalDate.of(2026, 7, 29) }.type in periodLikeTypes)
        assertFalse(julyDays.first { it.date == LocalDate.of(2026, 7, 30) }.type in periodLikeTypes)
        assertFalse(julyDays.first { it.date == LocalDate.of(2026, 7, 31) }.type in periodLikeTypes)
        assertEquals(CalendarDayType.LATE_PERIOD, augustDays.first { it.date == LocalDate.of(2026, 8, 1) }.type)
        assertEquals(CalendarDayType.LATE_PERIOD, augustDays.first { it.date == LocalDate.of(2026, 8, 2) }.type)
        assertEquals(CalendarDayType.PREDICTED_PERIOD, augustDays.first { it.date == LocalDate.of(2026, 8, 3) }.type)
        assertEquals(CalendarDayType.PREDICTED_PERIOD, augustDays.first { it.date == LocalDate.of(2026, 8, 4) }.type)
        assertEquals(CalendarDayType.PREDICTED_PERIOD, augustDays.first { it.date == LocalDate.of(2026, 8, 5) }.type)
        assertEquals(CalendarDayType.PREDICTED_PERIOD, augustDays.first { it.date == LocalDate.of(2026, 8, 6) }.type)
        assertEquals(CalendarDayType.PREDICTED_PERIOD, augustDays.first { it.date == LocalDate.of(2026, 8, 7) }.type)
    }

    @Test
    fun `late unconfirmed period moves again on the next day`() {
        val prediction = useCase(
            settings = CycleSettings(
                lastPeriodStart = LocalDate.of(2026, 7, 1),
                cycleLength = 28,
                periodLength = 5
            ),
            today = LocalDate.of(2026, 8, 3)
        )

        assertEquals(LocalDate.of(2026, 8, 4), prediction.nextPeriodStart)
        assertEquals(LocalDate.of(2026, 8, 8), prediction.nextPeriodEnd)
        assertEquals(5, prediction.lateDays)
        assertTrue(prediction.isLate)
    }

    @Test
    fun `calendar does not predict cycles before latest period start`() {
        val days = useCase.buildCalendarDays(
            settings = CycleSettings(
                lastPeriodStart = LocalDate.of(2026, 7, 1),
                cycleLength = 28,
                periodLength = 5
            ),
            monthStart = LocalDate.of(2026, 6, 1),
            loggedDates = emptySet(),
            today = LocalDate.of(2026, 7, 10)
        )

        assertEquals(CalendarDayType.NORMAL, days.first { it.date == LocalDate.of(2026, 6, 3) }.type)
        assertEquals(CalendarDayType.NORMAL, days.first { it.date == LocalDate.of(2026, 6, 17) }.type)
    }

    @Test
    fun `fertility window returns three days before and after selected date`() {
        val estimates = useCase.buildFertilityWindow(
            settings = CycleSettings(
                lastPeriodStart = LocalDate.of(2026, 7, 1),
                cycleLength = 28,
                periodLength = 5
            ),
            centerDate = LocalDate.of(2026, 7, 14),
            today = LocalDate.of(2026, 7, 10)
        )

        assertEquals(LocalDate.of(2026, 7, 11), estimates.first().date)
        assertEquals(LocalDate.of(2026, 7, 17), estimates.last().date)
        assertEquals(7, estimates.size)
        assertEquals(32, estimates.first { it.date == LocalDate.of(2026, 7, 14) }.probabilityPercent)
        assertEquals(FertilityLevel.PEAK, estimates.first { it.date == LocalDate.of(2026, 7, 14) }.level)
        assertEquals(5, estimates.first { it.date == LocalDate.of(2026, 7, 16) }.probabilityPercent)
    }

    private companion object {
        val periodLikeTypes = setOf(
            CalendarDayType.PERIOD,
            CalendarDayType.PREDICTED_PERIOD,
            CalendarDayType.LATE_PERIOD
        )
    }
}
