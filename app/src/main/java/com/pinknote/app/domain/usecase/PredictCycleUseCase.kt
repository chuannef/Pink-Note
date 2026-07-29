package com.pinknote.app.domain.usecase

import com.pinknote.app.domain.model.CalendarDay
import com.pinknote.app.domain.model.CalendarDayType
import com.pinknote.app.domain.model.CyclePrediction
import com.pinknote.app.domain.model.CycleSettings
import com.pinknote.app.domain.model.DailyLog
import com.pinknote.app.domain.model.FertilityEstimate
import com.pinknote.app.domain.model.FertilityLevel
import com.pinknote.app.domain.model.PredictionConfidence
import com.pinknote.app.utils.Constants
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt

class PredictCycleUseCase @Inject constructor() {

    operator fun invoke(
        settings: CycleSettings,
        today: LocalDate = LocalDate.now(),
        logs: List<DailyLog> = emptyList()
    ): CyclePrediction {
        val history = buildCycleHistory(settings, logs)
        val cycleLengths = history.periodStarts.zipWithNext { start, next ->
            ChronoUnit.DAYS.between(start, next).toInt()
        }
        val weightedCycleLength = weightedAverage(cycleLengths, settings.cycleLength)
            .coerceAtLeast(settings.periodLength + 1)
        val weightedPeriodLength = weightedAverage(history.periodDurations, settings.periodLength)
            .coerceAtLeast(1)
        val variabilityDays = calculateVariability(cycleLengths)
        val confidence = calculateConfidence(cycleLengths.size, variabilityDays)
        val currentPeriodStart = history.periodStarts.lastOrNull() ?: settings.lastPeriodStart
        val nextPeriodStart = currentPeriodStart.plusDays(weightedCycleLength.toLong())
        val nextPeriodEnd = nextPeriodStart.plusDays(weightedPeriodLength.toLong() - 1)
        val ovulationDate = nextPeriodStart.minusDays(14)
        val fertilePadding = (variabilityDays / 3).coerceIn(0, 4)
        val fertileStart = ovulationDate.minusDays(Constants.FERTILE_WINDOW_START_OFFSET.toLong() + fertilePadding)
        val fertileEnd = ovulationDate.plusDays(Constants.FERTILE_WINDOW_END_OFFSET.toLong() + fertilePadding)
        val pmsStart = nextPeriodStart.minusDays(7 + fertilePadding.toLong())
        val pmsEnd = nextPeriodStart.minusDays(1)
        val isLate = today.isAfter(nextPeriodStart) && history.periodStarts.none { !it.isBefore(nextPeriodStart) }
        val lateDays = if (isLate) ChronoUnit.DAYS.between(nextPeriodStart, today).toInt() else 0
        val todayType = resolveTypeForDate(
            date = today,
            periodConfirmation = logs.firstOrNull { it.date == today }?.isPeriodDay,
            currentPeriodStart = currentPeriodStart,
            currentPeriodEnd = currentPeriodStart.plusDays(weightedPeriodLength.toLong() - 1),
            predictedPeriodStart = nextPeriodStart,
            predictedPeriodEnd = nextPeriodEnd,
            ovulationDate = ovulationDate,
            fertileStart = fertileStart,
            fertileEnd = fertileEnd,
            pmsStart = pmsStart,
            pmsEnd = pmsEnd,
            today = today,
            isLate = isLate
        )
        val cycleDay = ChronoUnit.DAYS.between(currentPeriodStart, today).toInt() + 1
        val cycleProgress = (cycleDay.toFloat() / weightedCycleLength).coerceIn(0f, 1f)
        val fertilityEstimates = buildFertilityEstimates(ovulationDate, fertilePadding)
        val fertilityToday = fertilityEstimates.firstOrNull { it.date == today }
            ?: FertilityEstimate(today, farFromOvulationProbability(today, ovulationDate), FertilityLevel.VERY_LOW)

        return CyclePrediction(
            nextPeriodStart = nextPeriodStart,
            nextPeriodEnd = nextPeriodEnd,
            ovulationDate = ovulationDate,
            fertileStart = fertileStart,
            fertileEnd = fertileEnd,
            todayType = todayType,
            cycleDay = cycleDay,
            countdownText = buildCountdownText(today, currentPeriodStart, nextPeriodStart, ovulationDate, todayType, isLate, lateDays),
            confidence = confidence,
            weightedCycleLength = weightedCycleLength,
            weightedPeriodLength = weightedPeriodLength,
            cycleVariabilityDays = variabilityDays,
            isLate = isLate,
            lateDays = lateDays,
            pmsStart = pmsStart,
            pmsEnd = pmsEnd,
            cycleProgress = cycleProgress,
            historyCycleCount = cycleLengths.size,
            fertilityTodayPercent = fertilityToday.probabilityPercent,
            fertilityTodayLevel = fertilityToday.level,
            fertilityEstimates = fertilityEstimates,
            uncertaintyText = buildUncertaintyText(confidence, cycleLengths.size, variabilityDays),
            lateReasons = listOf("Stress", "Travel", "Hormonal changes", "Illness", "Lifestyle changes")
        )
    }

    fun buildCalendarDays(
        settings: CycleSettings,
        monthStart: LocalDate,
        loggedDates: Set<LocalDate>,
        periodConfirmations: Map<LocalDate, Boolean?> = emptyMap(),
        today: LocalDate = LocalDate.now(),
        logs: List<DailyLog> = emptyList()
    ): List<CalendarDay> {
        val prediction = invoke(settings, today, logs)
        val firstDay = monthStart.withDayOfMonth(1)
        val endDay = firstDay.plusMonths(1).minusDays(1)
        val currentPeriodStart = prediction.nextPeriodStart.minusDays(prediction.weightedCycleLength.toLong())
        val periodStarts = buildPeriodStartsForCalendar(
            currentPeriodStart = currentPeriodStart,
            cycleLength = prediction.weightedCycleLength,
            periodLength = prediction.weightedPeriodLength,
            firstDay = firstDay,
            endDay = endDay,
            fertilePadding = (prediction.cycleVariabilityDays / 3).coerceIn(0, 4)
        )
        return generateSequence(firstDay) { date ->
            if (date < endDay) date.plusDays(1) else null
        }.map { date ->
            CalendarDay(
                date = date,
                type = resolveTypeForCalendarDate(
                    date = date,
                    periodConfirmation = periodConfirmations[date],
                    currentPeriodStart = currentPeriodStart,
                    nextPeriodStart = prediction.nextPeriodStart,
                    periodStarts = periodStarts,
                    periodLength = prediction.weightedPeriodLength,
                    fertilePadding = (prediction.cycleVariabilityDays / 3).coerceIn(0, 4),
                    today = today,
                    isLate = prediction.isLate
                ),
                hasLog = loggedDates.contains(date)
            )
        }.toList()
    }

    fun buildFertilityWindow(
        settings: CycleSettings,
        centerDate: LocalDate,
        today: LocalDate = LocalDate.now(),
        logs: List<DailyLog> = emptyList()
    ): List<FertilityEstimate> {
        val prediction = invoke(settings, today, logs)
        val currentPeriodStart = prediction.nextPeriodStart.minusDays(prediction.weightedCycleLength.toLong())
        return (-3..3).map { dayOffset ->
            val date = centerDate.plusDays(dayOffset.toLong())
            val ovulationDate = ovulationDateForCycleContaining(
                date = date,
                currentPeriodStart = currentPeriodStart,
                cycleLength = prediction.weightedCycleLength
            )
            val ovulationOffset = ChronoUnit.DAYS.between(ovulationDate, date).toInt()
            val probability = probabilityForOvulationOffset(ovulationOffset)
            FertilityEstimate(
                date = date,
                probabilityPercent = probability,
                level = fertilityLevel(probability, ovulationOffset)
            )
        }
    }

    private fun ovulationDateForCycleContaining(
        date: LocalDate,
        currentPeriodStart: LocalDate,
        cycleLength: Int
    ): LocalDate {
        val cycleDays = cycleLength.toLong()
        var nextPeriodStart = currentPeriodStart
        while (!nextPeriodStart.isAfter(date)) {
            nextPeriodStart = nextPeriodStart.plusDays(cycleDays)
        }
        while (nextPeriodStart.minusDays(cycleDays).isAfter(date)) {
            nextPeriodStart = nextPeriodStart.minusDays(cycleDays)
        }
        return nextPeriodStart.minusDays(14)
    }

    private fun buildPeriodStartsForCalendar(
        currentPeriodStart: LocalDate,
        cycleLength: Int,
        periodLength: Int,
        firstDay: LocalDate,
        endDay: LocalDate,
        fertilePadding: Int
    ): List<LocalDate> {
        val cycleDays = cycleLength.toLong()
        val lookAheadDays = 14L + Constants.FERTILE_WINDOW_START_OFFSET + fertilePadding + periodLength
        var periodStart = currentPeriodStart

        val periodStarts = mutableListOf<LocalDate>()
        val scheduleEnd = endDay.plusDays(lookAheadDays)
        while (!periodStart.isAfter(scheduleEnd)) {
            periodStarts.add(periodStart)
            periodStart = periodStart.plusDays(cycleDays)
        }
        return periodStarts
    }

    private fun resolveTypeForCalendarDate(
        date: LocalDate,
        periodConfirmation: Boolean?,
        currentPeriodStart: LocalDate,
        nextPeriodStart: LocalDate,
        periodStarts: List<LocalDate>,
        periodLength: Int,
        fertilePadding: Int,
        today: LocalDate,
        isLate: Boolean
    ): CalendarDayType {
        val predictedType = resolveScheduledTypeForDate(
            date = date,
            currentPeriodStart = currentPeriodStart,
            nextPeriodStart = nextPeriodStart,
            periodStarts = periodStarts,
            periodLength = periodLength,
            fertilePadding = fertilePadding,
            today = today,
            isLate = isLate
        )

        return when (periodConfirmation) {
            true -> CalendarDayType.PERIOD
            false -> if (predictedType in periodLikeTypes) CalendarDayType.NORMAL else predictedType
            null -> predictedType
        }
    }

    private fun resolveScheduledTypeForDate(
        date: LocalDate,
        currentPeriodStart: LocalDate,
        nextPeriodStart: LocalDate,
        periodStarts: List<LocalDate>,
        periodLength: Int,
        fertilePadding: Int,
        today: LocalDate,
        isLate: Boolean
    ): CalendarDayType {
        if (date.isBefore(currentPeriodStart)) {
            return CalendarDayType.NORMAL
        }

        periodStarts.forEach { periodStart ->
            val periodEnd = periodStart.plusDays(periodLength.toLong() - 1)
            if (!date.isBefore(periodStart) && !date.isAfter(periodEnd)) {
                return when {
                    periodStart == currentPeriodStart -> CalendarDayType.PERIOD
                    isLate && periodStart == nextPeriodStart && !date.isAfter(today) -> CalendarDayType.LATE_PERIOD
                    else -> CalendarDayType.PREDICTED_PERIOD
                }
            }
        }

        if (isLate && !date.isBefore(nextPeriodStart) && !date.isAfter(today)) {
            return CalendarDayType.LATE_PERIOD
        }

        periodStarts.forEach { periodStart ->
            val ovulationDate = periodStart.minusDays(14)
            val fertileStart = ovulationDate.minusDays(Constants.FERTILE_WINDOW_START_OFFSET.toLong() + fertilePadding)
            val fertileEnd = ovulationDate.plusDays(Constants.FERTILE_WINDOW_END_OFFSET.toLong() + fertilePadding)
            val pmsStart = periodStart.minusDays(7 + fertilePadding.toLong())
            val pmsEnd = periodStart.minusDays(1)

            when {
                date == ovulationDate -> return CalendarDayType.OVULATION
                !date.isBefore(fertileStart) && !date.isAfter(fertileEnd) -> return CalendarDayType.FERTILE
                !date.isBefore(pmsStart) && !date.isAfter(pmsEnd) -> return CalendarDayType.PMS
            }
        }

        return CalendarDayType.NORMAL
    }

    private fun resolveTypeForDate(
        date: LocalDate,
        periodConfirmation: Boolean?,
        currentPeriodStart: LocalDate,
        currentPeriodEnd: LocalDate,
        predictedPeriodStart: LocalDate,
        predictedPeriodEnd: LocalDate,
        ovulationDate: LocalDate,
        fertileStart: LocalDate,
        fertileEnd: LocalDate,
        pmsStart: LocalDate,
        pmsEnd: LocalDate,
        today: LocalDate,
        isLate: Boolean
    ): CalendarDayType {
        val predictedType = when {
            !date.isBefore(currentPeriodStart) && !date.isAfter(currentPeriodEnd) -> CalendarDayType.PERIOD
            isLate && !date.isBefore(predictedPeriodStart) && !date.isAfter(today) -> CalendarDayType.LATE_PERIOD
            !date.isBefore(predictedPeriodStart) && !date.isAfter(predictedPeriodEnd) -> CalendarDayType.PREDICTED_PERIOD
            date == ovulationDate -> CalendarDayType.OVULATION
            !date.isBefore(fertileStart) && !date.isAfter(fertileEnd) -> CalendarDayType.FERTILE
            !date.isBefore(pmsStart) && !date.isAfter(pmsEnd) -> CalendarDayType.PMS
            else -> CalendarDayType.NORMAL
        }

        return when (periodConfirmation) {
            true -> CalendarDayType.PERIOD
            false -> if (predictedType in periodLikeTypes) CalendarDayType.NORMAL else predictedType
            null -> predictedType
        }
    }

    private fun buildCountdownText(
        today: LocalDate,
        currentPeriodStart: LocalDate,
        nextPeriodStart: LocalDate,
        ovulationDate: LocalDate,
        todayType: CalendarDayType,
        isLate: Boolean,
        lateDays: Int
    ): String {
        if (isLate) {
            return "Kỳ kinh có vẻ trễ $lateDays ngày. Hãy cập nhật khi kỳ kinh thực sự bắt đầu."
        }

        return when (todayType) {
            CalendarDayType.PERIOD -> {
                val day = ChronoUnit.DAYS.between(currentPeriodStart, today).toInt() + 1
                "Hôm nay là ngày thứ $day của kỳ kinh"
            }
            CalendarDayType.OVULATION -> "Hôm nay gần ngày rụng trứng ước tính"
            CalendarDayType.PREDICTED_PERIOD -> "Kỳ kinh dự kiến có thể bắt đầu trong hôm nay"
            CalendarDayType.PMS -> "Bạn có thể đang ở giai đoạn tiền kinh nguyệt"
            else -> {
                val daysToPeriod = ChronoUnit.DAYS.between(today, nextPeriodStart)
                val daysToOvulation = ChronoUnit.DAYS.between(today, ovulationDate)
                if (today.isBefore(ovulationDate)) {
                    "Còn khoảng $daysToOvulation ngày đến rụng trứng ước tính"
                } else {
                    "Còn khoảng $daysToPeriod ngày đến kỳ kinh dự kiến"
                }
            }
        }
    }

    private fun buildCycleHistory(settings: CycleSettings, logs: List<DailyLog>): CycleHistory {
        val confirmedPeriodDates = logs
            .filter { it.isPeriodDay == true }
            .map { it.date }
            .sorted()

        val periodGroups = confirmedPeriodDates.fold(mutableListOf<MutableList<LocalDate>>()) { groups, date ->
            val lastGroup = groups.lastOrNull()
            if (lastGroup == null || lastGroup.last().plusDays(1) != date) {
                groups.add(mutableListOf(date))
            } else {
                lastGroup.add(date)
            }
            groups
        }

        val starts = (periodGroups.mapNotNull { it.firstOrNull() } + settings.lastPeriodStart)
            .distinct()
            .sorted()
        val durations = periodGroups.map { it.size }.ifEmpty { listOf(settings.periodLength) }
        return CycleHistory(starts, durations)
    }

    private fun weightedAverage(values: List<Int>, fallback: Int): Int {
        val recent = values.takeLast(4).reversed()
        if (recent.isEmpty()) return fallback

        val weights = listOf(0.4, 0.3, 0.2, 0.1).take(recent.size)
        val weightSum = weights.sum()
        return recent.zip(weights).sumOf { (value, weight) -> value * weight }
            .div(weightSum)
            .roundToInt()
    }

    private fun calculateVariability(cycleLengths: List<Int>): Int {
        return if (cycleLengths.size < 2) 0 else cycleLengths.max() - cycleLengths.min()
    }

    private fun calculateConfidence(historyCount: Int, variabilityDays: Int): PredictionConfidence {
        return when {
            historyCount == 0 -> PredictionConfidence.VERY_LOW
            variabilityDays >= 10 -> PredictionConfidence.LOW
            historyCount >= 3 && variabilityDays <= 3 -> PredictionConfidence.HIGH
            historyCount >= 2 && variabilityDays <= 7 -> PredictionConfidence.MEDIUM
            else -> PredictionConfidence.LOW
        }
    }

    private fun buildFertilityEstimates(ovulationDate: LocalDate, fertilePadding: Int): List<FertilityEstimate> {
        val startOffset = -Constants.FERTILE_WINDOW_START_OFFSET - fertilePadding
        val endOffset = Constants.FERTILE_WINDOW_END_OFFSET + fertilePadding
        return (startOffset..endOffset).map { offset ->
            val probability = probabilityForOvulationOffset(offset)
            FertilityEstimate(
                date = ovulationDate.plusDays(offset.toLong()),
                probabilityPercent = probability,
                level = fertilityLevel(probability, offset)
            )
        }
    }

    private fun probabilityForOvulationOffset(offset: Int): Int {
        return when (offset) {
            -5 -> 8
            -4 -> 18
            -3 -> 28
            -2 -> 35
            -1 -> 42
            0 -> 38
            1 -> 10
            else -> if (offset in -9..4) 5 else 1
        }
    }

    private fun farFromOvulationProbability(today: LocalDate, ovulationDate: LocalDate): Int {
        return if (abs(ChronoUnit.DAYS.between(today, ovulationDate).toInt()) <= 10) 2 else 1
    }

    private fun fertilityLevel(probability: Int, offset: Int): FertilityLevel {
        return when {
            offset == -1 -> FertilityLevel.PEAK
            probability >= 35 -> FertilityLevel.VERY_HIGH
            probability >= 25 -> FertilityLevel.HIGH
            probability >= 10 -> FertilityLevel.MEDIUM
            probability >= 3 -> FertilityLevel.LOW
            else -> FertilityLevel.VERY_LOW
        }
    }

    private fun buildUncertaintyText(
        confidence: PredictionConfidence,
        historyCount: Int,
        variabilityDays: Int
    ): String {
        return when (confidence) {
            PredictionConfidence.VERY_LOW -> "Độ tin cậy rất thấp vì PinkNote mới có ít dữ liệu chu kỳ của bạn."
            PredictionConfidence.LOW -> "Độ tin cậy thấp. Chu kỳ gần đây còn biến động khoảng $variabilityDays ngày."
            PredictionConfidence.MEDIUM -> "Độ tin cậy trung bình dựa trên $historyCount chu kỳ đã xác nhận."
            PredictionConfidence.HIGH -> "Độ tin cậy cao hơn vì các chu kỳ gần đây khá đều, nhưng vẫn chỉ là ước tính."
        }
    }

    private data class CycleHistory(
        val periodStarts: List<LocalDate>,
        val periodDurations: List<Int>
    )

    private companion object {
        val periodLikeTypes = setOf(
            CalendarDayType.PERIOD,
            CalendarDayType.PREDICTED_PERIOD,
            CalendarDayType.LATE_PERIOD
        )
    }
}
