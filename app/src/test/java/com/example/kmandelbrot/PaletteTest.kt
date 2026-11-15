package com.example.kmandelbrot

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PaletteTest {

    private val red = 0xFFFF0000.toInt()
    private val yellow = 0xFFFFFF00.toInt()

    private fun green(color: Int): Int = (color shr 8) and 0xFF

    @Test
    fun gradientHas256ColoursFromRedToYellow() {
        val gradient = redToYellowGradient()

        assertEquals(256, gradient.size)
        assertEquals(red, gradient.first())
        assertEquals(yellow, gradient.last())
    }

    @Test
    fun gradientColoursAreOpaqueWithFullRedNoBlueAndGreenEqualToTheIndex() {
        val gradient = redToYellowGradient()

        for (index in gradient.indices) {
            assertEquals("colour $index", red or (index shl 8), gradient[index])
        }
    }

    @Test
    fun iterationColorsHasOneEntryPerPossibleResult() {
        assertEquals(MAX_ITERATIONS + 1, iterationColors().size)
    }

    @Test
    fun pointsInTheSetAreBlack() {
        assertEquals(COLOR_IN_SET, iterationColors()[NOT_ESCAPED])
        assertEquals(0xFF000000.toInt(), COLOR_IN_SET)
    }

    @Test
    fun fastestEscapeIsRedAndSlowestEscapeIsYellow() {
        val colors = iterationColors()

        assertEquals(red, colors[1])
        assertEquals(yellow, colors[MAX_ITERATIONS])
    }

    @Test
    fun coloursFollowTheLogarithmicCurve() {
        val colors = iterationColors()

        // Green equals the gradient index: 255 · ln(n) / ln(256), rounded.
        assertEquals(32, green(colors[2]))
        assertEquals(64, green(colors[4]))
        assertEquals(96, green(colors[8]))
        assertEquals(159, green(colors[32]))
        assertEquals(223, green(colors[128]))
    }

    @Test
    fun coloursNeverGetRedderAsIterationsIncrease() {
        val colors = iterationColors()

        for (iterations in 2..MAX_ITERATIONS) {
            assertTrue(
                "iterations $iterations",
                green(colors[iterations]) >= green(colors[iterations - 1]),
            )
        }
    }

    @Test
    fun endPointsHoldForAnotherIterationLimit() {
        val colors = iterationColors(maxIterations = 100)

        assertEquals(101, colors.size)
        assertEquals(COLOR_IN_SET, colors[NOT_ESCAPED])
        assertEquals(red, colors[1])
        assertEquals(yellow, colors[100])
    }
}
