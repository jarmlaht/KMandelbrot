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

    // A zoomed-in view near the middle of the set's rectangle, far from every limit.
    private val zoomedIn = Viewport(centerRe = -0.7, centerIm = 0.2, scale = 0.0005, width = 1000, height = 2000)

    @Test
    fun dragMovesTheCentreAgainstTheDragDirection() {
        // Drag 100 px right and 40 px down: the content follows the finger, so the view moves left and up.
        val dragged = zoomedIn.transformedBy(focusX = 600.0, focusY = 1040.0, panX = 100.0, panY = 40.0, zoom = 1.0)

        assertEquals(-0.7 - 100 * 0.0005, dragged.centerRe, delta)
        assertEquals(0.2 + 40 * 0.0005, dragged.centerIm, delta)
        assertEquals(0.0005, dragged.scale, delta)
    }

    @Test
    fun dragKeepsThePointUnderTheFingerUnderTheFinger() {
        val dragged = zoomedIn.transformedBy(focusX = 600.0, focusY = 1040.0, panX = 100.0, panY = 40.0, zoom = 1.0)

        assertEquals(zoomedIn.reAt(500.0), dragged.reAt(600.0), delta)
        assertEquals(zoomedIn.imAt(1000.0), dragged.imAt(1040.0), delta)
    }

    @Test
    fun pinchDividesTheScaleByTheZoomFactor() {
        val zoomed = zoomedIn.transformedBy(focusX = 300.0, focusY = 700.0, panX = 0.0, panY = 0.0, zoom = 2.0)

        assertEquals(0.00025, zoomed.scale, delta)
    }

    @Test
    fun pinchKeepsThePointUnderTheFingersFixed() {
        val zoomed = zoomedIn.transformedBy(focusX = 300.0, focusY = 700.0, panX = 0.0, panY = 0.0, zoom = 2.0)

        assertEquals(zoomedIn.reAt(300.0), zoomed.reAt(300.0), delta)
        assertEquals(zoomedIn.imAt(700.0), zoomed.imAt(700.0), delta)
    }

    @Test
    fun zoomingOutStopsWhenTheWholeSetFits() {
        val zoomedOut = zoomedIn.transformedBy(focusX = 500.0, focusY = 1000.0, panX = 0.0, panY = 0.0, zoom = 0.001)

        assertEquals(Viewport.fitWholeSet(1000, 2000).scale, zoomedOut.scale, delta)
    }

    @Test
    fun zoomingInStopsAtTheMinimumScale() {
        val deep = zoomedIn.copy(scale = Viewport.MIN_SCALE * 2)

        val zoomed = deep.transformedBy(focusX = 500.0, focusY = 1000.0, panX = 0.0, panY = 0.0, zoom = 100.0)

        assertEquals(Viewport.MIN_SCALE, zoomed.scale, 0.0)
    }

    @Test
    fun centreCannotBeDraggedOutOfTheSetRectangle() {
        val farRightAndDown = zoomedIn.transformedBy(
            focusX = 0.0, focusY = 0.0, panX = -1e7, panY = -1e7, zoom = 1.0,
        )
        val farLeftAndUp = zoomedIn.transformedBy(
            focusX = 0.0, focusY = 0.0, panX = 1e7, panY = 1e7, zoom = 1.0,
        )

        assertEquals(Viewport.SET_MAX_RE, farRightAndDown.centerRe, 0.0)
        assertEquals(Viewport.SET_MIN_IM, farRightAndDown.centerIm, 0.0)
        assertEquals(Viewport.SET_MIN_RE, farLeftAndUp.centerRe, 0.0)
        assertEquals(Viewport.SET_MAX_IM, farLeftAndUp.centerIm, 0.0)
    }

    @Test
    fun clampedLeavesAViewWithinTheLimitsUnchanged() {
        assertEquals(zoomedIn, zoomedIn.clamped())
        assertEquals(Viewport.fitWholeSet(1080, 2400), Viewport.fitWholeSet(1080, 2400).clamped())
    }

    @Test
    fun resizeForRotationKeepsCentreAndScale() {
        val rotated = zoomedIn.resizedTo(width = 2000, height = 1000)

        assertEquals(Viewport(centerRe = -0.7, centerIm = 0.2, scale = 0.0005, width = 2000, height = 1000), rotated)
    }

    @Test
    fun resizeClampsTheScaleWhenTheWholeSetWouldNoLongerFillTheImage() {
        val wholeSet = Viewport.fitWholeSet(width = 400, height = 400)

        val larger = wholeSet.resizedTo(width = 800, height = 800)

        assertEquals(Viewport.fitWholeSet(800, 800).scale, larger.scale, delta)
    }

    @Test
    fun placementOfTheSameViewportIsTheIdentity() {
        assertEquals(ImagePlacement(left = 0.0, top = 0.0, scaleFactor = 1.0), zoomedIn.placementOf(zoomedIn))
    }

    @Test
    fun placementAfterADragIsShiftedByTheDrag() {
        val dragged = zoomedIn.transformedBy(focusX = 600.0, focusY = 1040.0, panX = 100.0, panY = 40.0, zoom = 1.0)

        val placement = dragged.placementOf(zoomedIn)

        assertEquals(100.0, placement.left, 1e-6)
        assertEquals(40.0, placement.top, 1e-6)
        assertEquals(1.0, placement.scaleFactor, delta)
    }

    @Test
    fun placementAfterAPinchMapsEveryPointToWhereTheNewViewportPutsIt() {
        val zoomed = zoomedIn.transformedBy(focusX = 300.0, focusY = 700.0, panX = 20.0, panY = -10.0, zoom = 2.5)

        val placement = zoomed.placementOf(zoomedIn)

        assertEquals(2.5, placement.scaleFactor, delta)
        // A pixel position in the old image, drawn with the placement, must show the same complex point.
        val oldX = 123.0
        val oldY = 1456.0
        val drawnX = placement.left + oldX * placement.scaleFactor
        val drawnY = placement.top + oldY * placement.scaleFactor
        assertEquals(zoomedIn.reAt(oldX), zoomed.reAt(drawnX), delta)
        assertEquals(zoomedIn.imAt(oldY), zoomed.imAt(drawnY), delta)
    }

    @Test
    fun placementAcrossARotationKeepsTheCentreInTheCentre() {
        val rotated = zoomedIn.resizedTo(width = 2000, height = 1000)

        val placement = rotated.placementOf(zoomedIn)

        // The old image's centre (500, 1000) lands on the new image's centre (1000, 500).
        assertEquals(1000.0, placement.left + 500.0 * placement.scaleFactor, 1e-6)
        assertEquals(500.0, placement.top + 1000.0 * placement.scaleFactor, 1e-6)
        assertEquals(1.0, placement.scaleFactor, delta)
    }

    private fun assertContainsWholeSet(viewport: Viewport) {
        assertTrue(viewport.re(0) < Viewport.SET_MIN_RE)
        assertTrue(viewport.re(viewport.width - 1) > Viewport.SET_MAX_RE)
        assertTrue(viewport.im(0) > Viewport.SET_MAX_IM)
        assertTrue(viewport.im(viewport.height - 1) < Viewport.SET_MIN_IM)
    }
}
