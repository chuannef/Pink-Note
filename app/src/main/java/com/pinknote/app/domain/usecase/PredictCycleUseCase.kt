package com.pinknote.app.domain.usecase

import com.pinknote.app.domain.model.CalendarDay
import com.pinknote.app.domain.model.CalendarDayType
import com.pinknote.app.domain.model.CyclePrediction
import com.pinknote.app.domain.model.CycleSettings
import com.pinknote.app.utils.Constants
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.max

class PredictCycleUseCase @Inject constructor() {

    operator fun invoke(settings: CycleSettings, today: LocalDate = LocalDate.now()): CyclePrediction {
        val normalizedCycleLength = max(settings.cycleLength, settings.periodLength + 1)
        val nextPeriodStart = calculateNextPeriodStart(settings.lastPeriodStart, normalizedCycleLength, today)
        val currentPeriodStart = nextPeriodStart.minusDays(normalizedCycleLength.toLong())
        val currentPeriodEnd = currentPeriodStart.plusDays(settings.periodLength.toLong() - 1)
        val nextPeriodEnd = nextPeriodStart.plusDays(settings.periodLength.toLong() - 1)
        val ovulationDate = nextPeriodStart.minusDays(14)
        val fertileStart = ovulationDate.minusDays(Constants.FERTILE_WINDOW_START_OFFSET.toLong())
        val fertileEnd = ovulationDate.plusDays(Constants.FERTILE_WINDOW_END_OFFSET.toLong())
        val todayType = resolveDayType(today, currentPeriodStart, currentPeriodEnd, fertileStart, fertileEnd, ovulationDate)
        val cycleDay = ChronoUnit.DAYS.between(currentPeriodStart, today).toInt() + 1

        return CyclePrediction(
            nextPeriodStart = nextPeriodStart,
            nextPeriodEnd = nextPeriodEnd,
            ovulationDate = ovulationDate,
            fertileStart = fertileStart,
            fertileEnd = fertileEnd,
            todayType = todayType,
            cycleDay = cycleDay,
            countdownText = buildCountdownText(today, currentPeriodStart, currentPeriodEnd, nextPeriodStart, ovulationDate, todayType)
        )
    }

    fun buildCalendarDays(
        settings: CycleSettings,
        monthStart: LocalDate,
        loggedDates: Set<LocalDate>,
        periodConfirmations: Map<LocalDate, Boolean?> = emptyMap()
    ): List<CalendarDay> {
        val firstDay = monthStart.withDayOfMonth(1)
        val endDay = firstDay.plusMonths(1).minusDays(1)
        return generateSequence(firstDay) { date ->
            if (date < endDay) date.plusDays(1) else null
        }.map { date ->
            CalendarDay(
                date = date,
                type = resolveTypeForDate(settings, date, periodConfirmations[date]),
                hasLog = loggedDates.contains(date)
            )
        }.toList()
    }

    private fun calculateNextPeriodStart(lastPeriodStart: LocalDate, cycleLength: Int, today: LocalDate): LocalDate {
        var next = lastPeriodStart
        while (!next.isAfter(today)) {
            next = next.plusDays(cycleLength.toLong())
        }
        return next
    }

    private fun resolveTypeForDate(
        settings: CycleSettings,
        date: LocalDate,
        periodConfirmation: Boolean?
    ): CalendarDayType {
        val nextPeriodStart = calculateNextPeriodStart(settings.lastPeriodStart, settings.cycleLength, date)
        val currentPeriodStart = nextPeriodStart.minusDays(settings.cycleLength.toLong())
        val currentPeriodEnd = currentPeriodStart.plusDays(settings.periodLength.toLong() - 1)
        val ovulationDate = nextPeriodStart.minusDays(14)
        val fertileStart = ovulationDate.minusDays(Constants.FERTILE_WINDOW_START_OFFSET.toLong())
        val fertileEnd = ovulationDate.plusDays(Constants.FERTILE_WINDOW_END_OFFSET.toLong())
        val predictedType = resolveDayType(date, currentPeriodStart, currentPeriodEnd, fertileStart, fertileEnd, ovulationDate)
        return when (periodConfirmation) {
            true -> CalendarDayType.PERIOD
            false -> if (predictedType == CalendarDayType.PERIOD) CalendarDayType.NORMAL else predictedType
            null -> predictedType
        }
    }

    private fun resolveDayType(
        date: LocalDate,
        periodStart: LocalDate,
        periodEnd: LocalDate,
        fertileStart: LocalDate,
        fertileEnd: LocalDate,
        ovulationDate: LocalDate
    ): CalendarDayType {
        return when {
            !date.isBefore(periodStart) && !date.isAfter(periodEnd) -> CalendarDayType.PERIOD
            date == ovulationDate -> CalendarDayType.OVULATION
            !date.isBefore(fertileStart) && !date.isAfter(fertileEnd) -> CalendarDayType.FERTILE
            else -> CalendarDayType.NORMAL
        }
    }

    private fun buildCountdownText(
        today: LocalDate,
        currentPeriodStart: LocalDate,
        currentPeriodEnd: LocalDate,
        nextPeriodStart: LocalDate,
        ovulationDate: LocalDate,
        todayType: CalendarDayType
    ): String {
        return when (todayType) {
            CalendarDayType.PERIOD -> {
                val day = ChronoUnit.DAYS.between(currentPeriodStart, today).toInt() + 1
                "Hôm nay là ngày thứ $day của kỳ kinh"
            }
            CalendarDayType.OVULATION -> "Hôm nay là ngày rụng trứng"
            else -> {
                val daysToPeriod = ChronoUnit.DAYS.between(today, nextPeriodStart)
                val daysToOvulation = ChronoUnit.DAYS.between(today, ovulationDate)
                if (today.isBefore(ovulationDate)) {
                    "Còn $daysToOvulation ngày nữa đến ngày rụng trứng"
                } else if (today.isAfter(currentPeriodEnd)) {
                    "Còn $daysToPeriod ngày nữa đến kỳ kinh"
                } else {
                    "Còn $daysToPeriod ngày nữa đến kỳ kinh"
                }
            }
        }
    }
}
