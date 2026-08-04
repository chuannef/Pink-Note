package com.pinknote.app.domain.usecase

import com.pinknote.app.domain.model.PregnancySettings
import com.pinknote.app.domain.model.PregnancySummary
import com.pinknote.app.domain.model.PregnancyTrimester
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class PredictPregnancyUseCase @Inject constructor() {
    operator fun invoke(
        settings: PregnancySettings,
        today: LocalDate = LocalDate.now()
    ): PregnancySummary? {
        val dueDate = settings.dueDate
            ?: settings.lastMenstrualPeriod?.plusDays(PREGNANCY_LENGTH_DAYS)
            ?: return null
        val estimatedLmp = settings.lastMenstrualPeriod ?: dueDate.minusDays(PREGNANCY_LENGTH_DAYS)
        val gestationalDays = ChronoUnit.DAYS.between(estimatedLmp, today)
            .toInt()
            .coerceAtLeast(0)
        val week = (gestationalDays / DAYS_PER_WEEK + 1).coerceAtMost(MAX_DISPLAY_WEEK)
        val dayOfWeek = gestationalDays % DAYS_PER_WEEK
        val daysUntilDue = ChronoUnit.DAYS.between(today, dueDate)
        val trimester = when {
            gestationalDays <= FIRST_TRIMESTER_LAST_DAY -> PregnancyTrimester.FIRST
            gestationalDays <= SECOND_TRIMESTER_LAST_DAY -> PregnancyTrimester.SECOND
            else -> PregnancyTrimester.THIRD
        }
        val progress = (gestationalDays.toFloat() / PREGNANCY_LENGTH_DAYS)
            .coerceIn(0f, 1f)
        val statusText = when {
            daysUntilDue > 0 -> "Con $daysUntilDue ngay den ngay du sinh"
            daysUntilDue == 0L -> "Hom nay la ngay du sinh uoc tinh"
            else -> "Da qua ngay du sinh ${-daysUntilDue} ngay"
        }

        return PregnancySummary(
            gestationalWeek = week,
            gestationalDayOfWeek = dayOfWeek,
            gestationalDays = gestationalDays,
            trimester = trimester,
            dueDate = dueDate,
            daysUntilDue = daysUntilDue,
            progress = progress,
            statusText = statusText
        )
    }

    private companion object {
        const val DAYS_PER_WEEK = 7
        const val PREGNANCY_LENGTH_DAYS = 280L
        const val MAX_DISPLAY_WEEK = 42
        const val FIRST_TRIMESTER_LAST_DAY = 13 * DAYS_PER_WEEK + 6
        const val SECOND_TRIMESTER_LAST_DAY = 27 * DAYS_PER_WEEK + 6
    }
}
