package com.example.kmandelbrot

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MandelbrotTest {

    @Test
    fun pointsInsideTheSetDoNotEscape() {
        assertEquals(NOT_ESCAPED, escapeIterations(0.0, 0.0))
        assertEquals(NOT_ESCAPED, escapeIterations(-1.0, 0.0))
        assertEquals(NOT_ESCAPED, escapeIterations(-2.0, 0.0))
        assertEquals(NOT_ESCAPED, escapeIterations(0.25, 0.0))
    }

    @Test
    fun pointOutsideRadiusTwoEscapesOnTheFirstIteration() {
        // z1 = c = 2 + 2i, |z1|² = 8
        assertEquals(1, escapeIterations(2.0, 2.0))
    }

    @Test
    fun pointsOutsideTheSetReturnTheIterationOnWhichTheyEscape() {
        // c = 1 + i: z1 = 1 + i (|z|² = 2), z2 = 1 + 3i (|z|² = 10)
        assertEquals(2, escapeIterations(1.0, 1.0))
        // c = 0.5: z = 0.5, 0.75, 1.0625, 1.6289…, 3.1533…
        assertEquals(5, escapeIterations(0.5, 0.0))
    }

    @Test
    fun slowlyEscapingPointNeedsEnoughIterations() {
        // 0.26 lies just outside the cusp at 0.25 and takes about 30 iterations to escape.
        assertEquals(NOT_ESCAPED, escapeIterations(0.26, 0.0, maxIterations = 10))
        assertTrue(escapeIterations(0.26, 0.0) > 10)
    }

    @Test
    fun pointEscapingOnTheLastAllowedIterationIsReported() {
        assertEquals(5, escapeIterations(0.5, 0.0, maxIterations = 5))
        assertEquals(NOT_ESCAPED, escapeIterations(0.5, 0.0, maxIterations = 4))
    }

    @Test
    fun pointsInTheMainCardioidAndPeriod2BulbDoNotEscape() {
        assertEquals(NOT_ESCAPED, escapeIterations(-0.1, 0.1))
        assertEquals(NOT_ESCAPED, escapeIterations(0.2, 0.3))
        assertEquals(NOT_ESCAPED, escapeIterations(-1.0, 0.2))
        assertEquals(NOT_ESCAPED, escapeIterations(-1.2, 0.0))
    }

    @Test
    fun pointsJustOutsideTheCardioidAndBulbStillEscape() {
        // Between the cardioid and the period-2 bulb, just off the real axis.
        assertTrue(escapeIterations(-0.75, 0.1) > 0)
        // Just above the top of the period-2 bulb (radius 0.25).
        assertTrue(escapeIterations(-1.0, 0.3) > 0)
        // Just right of the cardioid's cusp.
        assertTrue(escapeIterations(0.26, 0.0) > 0)
    }

    @Test
    fun iterationLimitIsTheBaseLimitAtAndAboveTheReferenceScale() {
        assertEquals(BASE_ITERATION_LIMIT, iterationLimitForScale(0.0025))
        assertEquals(BASE_ITERATION_LIMIT, iterationLimitForScale(0.01))
    }

    @Test
    fun iterationLimitGrowsBy940ForEveryTenfoldMagnification() {
        assertEquals(1196, iterationLimitForScale(0.0025 / 10))
        assertEquals(3076, iterationLimitForScale(0.0025 / 1e3))
        assertEquals(5896, iterationLimitForScale(0.0025 / 1e6))
        assertEquals(8716, iterationLimitForScale(0.0025 / 1e9))
    }

    @Test
    fun iterationLimitNeverDecreasesAsTheScaleShrinks() {
        var scale = 0.01
        var previous = iterationLimitForScale(scale)
        while (scale > Viewport.MIN_SCALE) {
            scale /= 2
            val limit = iterationLimitForScale(scale)
            assertTrue("scale $scale", limit >= previous)
            previous = limit
        }
    }

    @Test
    fun iterationLimitIsCappedAtTheDeepestZoom() {
        assertEquals(ITERATION_LIMIT_CAP, iterationLimitForScale(Viewport.MIN_SCALE))
        assertEquals(ITERATION_LIMIT_CAP, iterationLimitForScale(1e-30))
    }
}
