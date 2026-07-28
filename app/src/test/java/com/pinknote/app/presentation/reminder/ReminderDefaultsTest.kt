package com.pinknote.app.presentation.reminder

import com.pinknote.app.domain.model.ReminderType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderDefaultsTest {

    @Test
    fun `default title and message match reminder type`() {
        val beforePeriod = ReminderDefaults.forType(ReminderType.BEFORE_PERIOD)
        val water = ReminderDefaults.forType(ReminderType.WATER)
        val workout = ReminderDefaults.forType(ReminderType.WORKOUT)

        assertEquals("Trước kỳ kinh", beforePeriod.label)
        assertEquals("Uống nước", water.title)
        assertEquals("Workout", workout.label)
        assertTrue(beforePeriod.message.isNotBlank())
    }
}
