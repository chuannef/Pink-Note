package com.pinknote.app.domain.usecase

import com.pinknote.app.domain.model.CalendarDayType
import com.pinknote.app.domain.model.CycleSettings
import org.junit.Assert.assertEquals
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
}
