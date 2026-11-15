package com.example.kmandelbrot

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MandelbrotTest {

    @Test
    fun pointsInsideTheSet() {
        assertTrue(isInMandelbrotSet(0.0, 0.0))
        assertTrue(isInMandelbrotSet(-1.0, 0.0))
        assertTrue(isInMandelbrotSet(-2.0, 0.0))
        assertTrue(isInMandelbrotSet(0.25, 0.0))
    }

    @Test
    fun pointsOutsideTheSet() {
        assertFalse(isInMandelbrotSet(1.0, 1.0))
        assertFalse(isInMandelbrotSet(0.5, 0.0))
        assertFalse(isInMandelbrotSet(2.0, 2.0))
    }

    @Test
    fun slowlyEscapingPointNeedsEnoughIterations() {
        // 0.26 lies just outside the cusp at 0.25 and takes about 30 iterations to escape.
        assertTrue(isInMandelbrotSet(0.26, 0.0, maxIterations = 10))
        assertFalse(isInMandelbrotSet(0.26, 0.0))
    }
}
