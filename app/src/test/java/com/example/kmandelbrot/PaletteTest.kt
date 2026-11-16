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
        val gradient = colorGradient(red, yellow)

        assertEquals(256, gradient.size)
        assertEquals(red, gradient.first())
        assertEquals(yellow, gradient.last())
    }

    @Test
    fun gradientColoursAreOpaqueWithFullRedNoBlueAndGreenEqualToTheIndex() {
        val gradient = colorGradient(red, yellow)

        for (index in gradient.indices) {
            assertEquals("colour $index", red or (index shl 8), gradient[index])
        }
    }

    @Test
    fun gradientBetweenOtherColoursStartsAndEndsOnThemWithTheRoundedMidpoint() {
        val start = 0xFF102030.toInt()
        val end = 0xFF3060F0.toInt()

        val gradient = colorGradient(start, end)

        assertEquals(256, gradient.size)
        assertEquals(start, gradient.first())
        assertEquals(end, gradient.last())
        // At index 128 of 255: red 16 + 32 · 128/255 = 32.06, green 32 + 64 · 128/255 = 64.13,
        // blue 48 + 192 · 128/255 = 144.38.
        assertEquals(0xFF204090.toInt(), gradient[128])
    }

    @Test
    fun gradientWithFallingChannelsRunsFromWhiteToBlack() {
        val gradient = colorGradient(0xFFFFFFFF.toInt(), 0xFF000000.toInt())

        for (index in gradient.indices) {
            val channel = 255 - index
            val expected = 0xFF000000.toInt() or (channel shl 16) or (channel shl 8) or channel
            assertEquals("colour $index", expected, gradient[index])
        }
    }

    @Test
    fun gradientBetweenIdenticalColoursIsThatColour() {
        val color = 0xFF336699.toInt()

        assertTrue(colorGradient(color, color).all { it == color })
    }

    @Test
    fun gradientColoursAreOpaqueEvenIfTheGivenColoursAreNot() {
        val gradient = colorGradient(0x00FF0000, 0x80FFFF00.toInt())

        assertEquals(red, gradient.first())
        assertEquals(yellow, gradient.last())
    }

    @Test
    fun iterationColorsUsesTheGivenPalette() {
        val blue = 0xFF0000FF.toInt()
        val white = 0xFFFFFFFF.toInt()

        val colors = iterationColors(maxIterations = 100, palette = PaletteColors(farColor = blue, edgeColor = white))

        assertEquals(COLOR_IN_SET, colors[NOT_ESCAPED])
        assertEquals(blue, colors[1])
        assertEquals(white, colors[100])
    }

    @Test
    fun defaultPaletteIsRedToYellow() {
        assertEquals(PaletteColors(farColor = red, edgeColor = yellow), PaletteColors())
    }

    @Test
    fun iterationColorsHasOneEntryPerPossibleResult() {
        assertEquals(BASE_ITERATION_LIMIT + 1, iterationColors().size)
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
        assertEquals(yellow, colors[BASE_ITERATION_LIMIT])
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

        for (iterations in 2..BASE_ITERATION_LIMIT) {
            assertTrue(
                "iterations $iterations",
                green(colors[iterations]) >= green(colors[iterations - 1]),
            )
        }
    }

    @Test
    fun endPointsHoldForTheHighestIterationLimit() {
        val colors = iterationColors(maxIterations = ITERATION_LIMIT_CAP)

        assertEquals(ITERATION_LIMIT_CAP + 1, colors.size)
        assertEquals(COLOR_IN_SET, colors[NOT_ESCAPED])
        assertEquals(red, colors[1])
        assertEquals(yellow, colors[ITERATION_LIMIT_CAP])
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
