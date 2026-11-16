package com.example.kmandelbrot

import org.junit.Assert.assertEquals
import org.junit.Test

class CoordinateFormatTest {

    @Test
    fun valueIsShownWithSixteenDecimals() {
        assertEquals("-0.7500000000000000", formatCoordinate(-0.75))
        assertEquals("0.0000000000000000", formatCoordinate(0.0))
    }

    @Test
    fun smallValueIsShownWithoutAnExponent() {
        assertEquals("0.0000123400000000", formatCoordinate(0.00001234))
    }

    @Test
    fun allSixteenDecimalsOfAValueAreShown() {
        assertEquals("-0.7436438870371587", formatCoordinate(-0.7436438870371587))
    }
}
