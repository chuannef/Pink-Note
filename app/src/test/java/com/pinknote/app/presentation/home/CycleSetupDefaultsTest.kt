package com.pinknote.app.presentation.home

import com.pinknote.app.utils.Constants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CycleSetupDefaultsTest {

    @Test
    fun `blank cycle and period inputs use average defaults`() {
        val result = CycleSetupDefaults.resolve(cycleLengthInput = "", periodLengthInput = "")

        assertEquals(Constants.DEFAULT_CYCLE_LENGTH, result.cycleLength)
        assertEquals(Constants.DEFAULT_PERIOD_LENGTH, result.periodLength)
        assertNull(result.error)
    }

    @Test
    fun `custom cycle input can be saved without period input`() {
        val result = CycleSetupDefaults.resolve(cycleLengthInput = "30", periodLengthInput = "")

        assertEquals(30, result.cycleLength)
        assertEquals(Constants.DEFAULT_PERIOD_LENGTH, result.periodLength)
        assertNull(result.error)
    }

    @Test
    fun `period length must stay shorter than resolved cycle length`() {
        val result = CycleSetupDefaults.resolve(cycleLengthInput = "", periodLengthInput = "28")

        assertEquals(CycleSetupInputError.PERIOD_SHORTER_THAN_CYCLE, result.error)
    }
}
