package com.pinknote.app.utils

import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(Constants.DATE_PATTERN, Locale.US)

    fun LocalDate.toStorageString(): String = format(formatter)

    fun parseStorageDate(value: String): LocalDate = LocalDate.parse(value, formatter)

    fun ageFromBirthday(birthday: LocalDate?): Int {
        return birthday?.let { Period.between(it, LocalDate.now()).years } ?: 0
    }
}
