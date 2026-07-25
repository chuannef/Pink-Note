package com.pinknote.app.presentation.home

fun formatCycleDateInput(rawInput: String, previousValue: String): String {
    val isDeletingAutoSeparator = previousValue.endsWith("-") &&
        rawInput == previousValue.dropLast(1)
    if (isDeletingAutoSeparator) return rawInput

    val digits = rawInput.filter(Char::isDigit).take(8)
    if (digits.length < 4) return digits
    if (digits.length == 4) return "$digits-"

    val year = digits.take(4)
    val monthAndDay = digits.drop(4)
    if (monthAndDay.length == 1) {
        val monthFirstDigit = monthAndDay.first()
        return if (monthFirstDigit in '2'..'9') {
            "$year-0$monthFirstDigit-"
        } else {
            "$year-$monthFirstDigit"
        }
    }

    val month = monthAndDay
        .take(2)
        .toInt()
        .coerceIn(MIN_MONTH, MAX_MONTH)
        .toString()
        .padStart(2, '0')
    val day = monthAndDay.drop(2).take(2)
    return if (day.isEmpty()) "$year-$month-" else "$year-$month-$day"
}

private const val MIN_MONTH = 1
private const val MAX_MONTH = 12
