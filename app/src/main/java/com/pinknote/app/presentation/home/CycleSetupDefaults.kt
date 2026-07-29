package com.pinknote.app.presentation.home

import com.pinknote.app.utils.Constants

enum class CycleSetupInputError {
    INVALID_CYCLE_LENGTH,
    INVALID_PERIOD_LENGTH,
    PERIOD_SHORTER_THAN_CYCLE
}

data class CycleSetupInputResult(
    val cycleLength: Int = Constants.DEFAULT_CYCLE_LENGTH,
    val periodLength: Int = Constants.DEFAULT_PERIOD_LENGTH,
    val error: CycleSetupInputError? = null
)

object CycleSetupDefaults {
    fun resolve(cycleLengthInput: String, periodLengthInput: String): CycleSetupInputResult {
        val cycleLength = parseOptionalPositiveInt(
            value = cycleLengthInput,
            fallback = Constants.DEFAULT_CYCLE_LENGTH,
            error = CycleSetupInputError.INVALID_CYCLE_LENGTH
        )
        val periodLength = parseOptionalPositiveInt(
            value = periodLengthInput,
            fallback = Constants.DEFAULT_PERIOD_LENGTH,
            error = CycleSetupInputError.INVALID_PERIOD_LENGTH
        )

        val inputError = cycleLength.error ?: periodLength.error
        if (inputError != null) {
            return CycleSetupInputResult(error = inputError)
        }

        return if (periodLength.value >= cycleLength.value) {
            CycleSetupInputResult(error = CycleSetupInputError.PERIOD_SHORTER_THAN_CYCLE)
        } else {
            CycleSetupInputResult(cycleLength = cycleLength.value, periodLength = periodLength.value)
        }
    }

    private fun parseOptionalPositiveInt(
        value: String,
        fallback: Int,
        error: CycleSetupInputError
    ): ParsedCycleNumber {
        if (value.isBlank()) {
            return ParsedCycleNumber(value = fallback)
        }

        val parsedValue = value.toIntOrNull()
        return if (parsedValue == null || parsedValue <= 0) {
            ParsedCycleNumber(value = fallback, error = error)
        } else {
            ParsedCycleNumber(value = parsedValue)
        }
    }

    private data class ParsedCycleNumber(
        val value: Int,
        val error: CycleSetupInputError? = null
    )
}
