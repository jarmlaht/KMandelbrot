package com.example.kmandelbrot

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class IterationSettingsTest {

    @Test
    fun defaultsAreScaleBasedWithTheBaseLimit() {
        val settings = IterationSettings()

        assertEquals(IterationMode.SCALE_BASED, settings.mode)
        assertEquals(BASE_ITERATION_LIMIT, settings.fixedLimit)
    }

    @Test
    fun scaleBasedModeUsesTheLimitForTheScaleAndIgnoresTheFixedLimit() {
        val settings = IterationSettings(mode = IterationMode.SCALE_BASED, fixedLimit = 5000)

        assertEquals(iterationLimitForScale(0.01), settings.limitFor(0.01))
        assertEquals(iterationLimitForScale(1e-6), settings.limitFor(1e-6))
    }

    @Test
    fun fixedModeUsesTheFixedLimitAtEveryScale() {
        val settings = IterationSettings(mode = IterationMode.FIXED, fixedLimit = 5000)

        assertEquals(5000, settings.limitFor(0.01))
        assertEquals(5000, settings.limitFor(Viewport.MIN_SCALE))
    }

    @Test
    fun fixedLimitOutsideTheAllowedRangeIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            IterationSettings(fixedLimit = MIN_FIXED_ITERATION_LIMIT - 1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            IterationSettings(fixedLimit = MAX_FIXED_ITERATION_LIMIT + 1)
        }
    }

    @Test
    fun parseAcceptsWholeNumbersInTheAllowedRange() {
        assertEquals(2, parseFixedIterationLimit("2"))
        assertEquals(256, parseFixedIterationLimit("256"))
        assertEquals(100_000, parseFixedIterationLimit("100000"))
    }

    @Test
    fun parseRejectsNumbersOutsideTheAllowedRange() {
        assertNull(parseFixedIterationLimit("0"))
        assertNull(parseFixedIterationLimit("1"))
        assertNull(parseFixedIterationLimit("100001"))
        assertNull(parseFixedIterationLimit("-5"))
    }

    @Test
    fun parseRejectsTextThatIsNotAnInt() {
        assertNull(parseFixedIterationLimit(""))
        assertNull(parseFixedIterationLimit("12a"))
        assertNull(parseFixedIterationLimit("1.5"))
        // Too large for an Int.
        assertNull(parseFixedIterationLimit("99999999999"))
    }
}
