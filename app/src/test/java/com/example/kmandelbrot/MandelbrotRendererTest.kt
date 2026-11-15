package com.example.kmandelbrot

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MandelbrotRendererTest {

    // 50 rows with bands of 16 gives three full bands and a partial last band of 2 rows.
    private val viewport = Viewport.fitWholeSet(width = 60, height = 50)

    private fun render(): Map<Int, IntArray> {
        val bands = mutableMapOf<Int, IntArray>()
        runBlocking {
            renderMandelbrot(viewport, bandRows = 16) { firstRow, pixels ->
                synchronized(bands) {
                    assertTrue("band at row $firstRow delivered twice", bands.put(firstRow, pixels) == null)
                }
            }
        }
        return bands
    }

    @Test
    fun bandsCoverEveryRowExactlyOnce() {
        val bands = render()

        assertEquals(setOf(0, 16, 32, 48), bands.keys)
        assertEquals(16 * 60, bands.getValue(0).size)
        assertEquals(16 * 60, bands.getValue(16).size)
        assertEquals(16 * 60, bands.getValue(32).size)
        assertEquals(2 * 60, bands.getValue(48).size)
    }

    @Test
    fun everyPixelMatchesTheSetMembershipOfItsCoordinates() {
        val bands = render()

        var insideCount = 0
        for ((firstRow, pixels) in bands) {
            for (i in pixels.indices) {
                val x = i % viewport.width
                val y = firstRow + i / viewport.width
                val inside = isInMandelbrotSet(viewport.re(x), viewport.im(y))
                assertEquals("pixel ($x, $y)", if (inside) COLOR_INSIDE else COLOR_OUTSIDE, pixels[i])
                if (inside) insideCount++
            }
        }
        // Guards against a degenerate viewport making the test pass trivially.
        assertTrue(insideCount > 0)
        assertTrue(insideCount < viewport.width * viewport.height)
    }
}
