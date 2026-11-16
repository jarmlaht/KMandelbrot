package com.example.kmandelbrot

import org.junit.Assert.assertEquals
import org.junit.Test

class PickerColorTest {

    private val black = 0xFF000000.toInt()
    private val white = 0xFFFFFFFF.toInt()
    private val red = 0xFFFF0000.toInt()
    private val yellow = 0xFFFFFF00.toInt()
    private val green = 0xFF00FF00.toInt()
    private val cyan = 0xFF00FFFF.toInt()
    private val blue = 0xFF0000FF.toInt()
    private val magenta = 0xFFFF00FF.toInt()

    private val tolerance = 1e-4f

    @Test
    fun middleToneGivesThePureColourOfTheHue() {
        assertEquals(red, pickerColor(hue = 0f, tone = 0.5f))
        assertEquals(yellow, pickerColor(hue = 60f, tone = 0.5f))
        assertEquals(green, pickerColor(hue = 120f, tone = 0.5f))
        assertEquals(cyan, pickerColor(hue = 180f, tone = 0.5f))
        assertEquals(blue, pickerColor(hue = 240f, tone = 0.5f))
        assertEquals(magenta, pickerColor(hue = 300f, tone = 0.5f))
    }

    @Test
    fun lowestToneIsBlackAndHighestToneIsWhiteForAnyHue() {
        for (hue in listOf(0f, 45f, 200f, 359f)) {
            assertEquals("hue $hue", black, pickerColor(hue, tone = 0f))
            assertEquals("hue $hue", white, pickerColor(hue, tone = 1f))
        }
    }

    @Test
    fun endOfTheHueRangeIsTheSameAsItsStart() {
        assertEquals(pickerColor(hue = 0f, tone = 0.5f), pickerColor(hue = 360f, tone = 0.5f))
        assertEquals(pickerColor(hue = 0f, tone = 0.3f), pickerColor(hue = 360f, tone = 0.3f))
    }

    @Test
    fun tonesBetweenBlendTowardsBlackAndWhite() {
        // Halfway from black to pure red, and halfway from pure red to white.
        assertEquals(0xFF800000.toInt(), pickerColor(hue = 0f, tone = 0.25f))
        assertEquals(0xFFFF8080.toInt(), pickerColor(hue = 0f, tone = 0.75f))
    }

    @Test
    fun hueBetweenTwoPureColoursBlendsThem() {
        // Orange is halfway between red and yellow.
        assertEquals(0xFFFF8000.toInt(), pickerColor(hue = 30f, tone = 0.5f))
    }

    @Test
    fun positionOfAPureColourIsItsHueAtTheMiddleTone() {
        val expectedHues = mapOf(red to 0f, yellow to 60f, green to 120f, cyan to 180f, blue to 240f, magenta to 300f)

        for ((color, hue) in expectedHues) {
            val position = pickerPositionOf(color)
            assertEquals(hue, position.hue, tolerance)
            assertEquals(0.5f, position.tone, tolerance)
        }
    }

    @Test
    fun positionOfBlackWhiteAndGreyHasHueZero() {
        assertEquals(PickerPosition(hue = 0f, tone = 0f), pickerPositionOf(black))
        assertEquals(PickerPosition(hue = 0f, tone = 1f), pickerPositionOf(white))

        val grey = pickerPositionOf(0xFF808080.toInt())
        assertEquals(0f, grey.hue, tolerance)
        assertEquals(128 / 255f, grey.tone, tolerance)
    }

    @Test
    fun positionOfAPickedColourGivesTheSameColourAgain() {
        for (hue in listOf(0f, 30f, 95f, 180f, 222f, 300f, 345f)) {
            for (tone in listOf(0.1f, 0.25f, 0.5f, 0.6f, 0.9f)) {
                val color = pickerColor(hue, tone)
                val position = pickerPositionOf(color)
                assertEquals("hue $hue tone $tone", color, pickerColor(position.hue, position.tone))
            }
        }
    }
}
