package com.example.kmandelbrot

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ViewportTest {

    private val delta = 1e-12

    @Test
    fun centreOfImageMapsToViewportCentre() {
        val viewport = Viewport(centerRe = -0.5, centerIm = 0.25, scale = 0.01, width = 200, height = 100)

        // The centre lies on the boundary between pixels 99 and 100 (columns) and 49 and 50 (rows).
        assertEquals(-0.5, (viewport.re(99) + viewport.re(100)) / 2, delta)
        assertEquals(0.25, (viewport.im(49) + viewport.im(50)) / 2, delta)
    }

    @Test
    fun adjacentPixelsAreOneScaleStepApartOnBothAxes() {
        val viewport = Viewport(centerRe = 0.0, centerIm = 0.0, scale = 0.01, width = 200, height = 100)

        assertEquals(0.01, viewport.re(11) - viewport.re(10), delta)
        assertEquals(0.01, viewport.im(10) - viewport.im(11), delta)
    }

    @Test
    fun imaginaryAxisPointsUp() {
        val viewport = Viewport(centerRe = 0.0, centerIm = 0.0, scale = 0.01, width = 200, height = 100)

        assertTrue(viewport.im(0) > 0)
        assertTrue(viewport.im(99) < 0)
    }

    @Test
    fun fitWholeSetContainsTheSetInLandscape() {
        assertContainsWholeSet(Viewport.fitWholeSet(width = 2400, height = 1080))
    }

    @Test
    fun fitWholeSetContainsTheSetInPortrait() {
        assertContainsWholeSet(Viewport.fitWholeSet(width = 1080, height = 2400))
    }

    @Test
    fun fitWholeSetIsCentredOnTheSet() {
        val viewport = Viewport.fitWholeSet(width = 1080, height = 2400)

        assertEquals(-0.75, viewport.centerRe, delta)
        assertEquals(0.0, viewport.centerIm, delta)
    }

    @Test
    fun fitWholeSetFillsTheLimitingDimension() {
        // The set's region is square, so the shorter side limits the scale in both orientations.
        val landscape = Viewport.fitWholeSet(width = 2400, height = 1080)
        val portrait = Viewport.fitWholeSet(width = 1080, height = 2400)

        assertEquals(landscape.scale, portrait.scale, delta)
        assertEquals(2.75, landscape.scale * 1080, delta)
    }

    private fun assertContainsWholeSet(viewport: Viewport) {
        assertTrue(viewport.re(0) < Viewport.SET_MIN_RE)
        assertTrue(viewport.re(viewport.width - 1) > Viewport.SET_MAX_RE)
        assertTrue(viewport.im(0) > Viewport.SET_MAX_IM)
        assertTrue(viewport.im(viewport.height - 1) < Viewport.SET_MIN_IM)
    }
}
