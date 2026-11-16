package com.example.kmandelbrot

/**
 * Maps pixels of a width × height image to the complex plane.
 * [scale] is complex units per pixel, the same on both axes, so the image is never stretched.
 */
data class Viewport(
    val centerRe: Double,
    val centerIm: Double,
    val scale: Double,
    val width: Int,
    val height: Int,
) {
    /** Real coordinate of the centre of pixel column [x]. */
    fun re(x: Int): Double = reAt(x + PIXEL_CENTER)

    /** Imaginary coordinate of the centre of pixel row [y]; row 0 is the top, so im decreases as y grows. */
    fun im(y: Int): Double = imAt(y + PIXEL_CENTER)

    /** Real coordinate at horizontal pixel position [x], where 0.0 is the left edge of the image. */
    fun reAt(x: Double): Double = centerRe + (x - width / 2.0) * scale

    /** Imaginary coordinate at vertical pixel position [y], where 0.0 is the top edge of the image. */
    fun imAt(y: Double): Double = centerIm - (y - height / 2.0) * scale

    /**
     * The viewport after one step of a pinch/drag gesture.
     * The complex point that was under the fingers' previous centre ([focusX] - [panX], [focusY] - [panY])
     * ends up under their current centre ([focusX], [focusY]), and the view is magnified by [zoom] around it.
     * The result is clamped with [clamped].
     */
    fun transformedBy(focusX: Double, focusY: Double, panX: Double, panY: Double, zoom: Double): Viewport {
        require(zoom > 0.0) { "zoom must be positive, was $zoom" }
        val anchorRe = reAt(focusX - panX)
        val anchorIm = imAt(focusY - panY)
        // Limit the scale before placing the centre, so the anchor stays under the fingers at the zoom limits.
        val newScale = (scale / zoom).coerceIn(MIN_SCALE, maxScale())
        return copy(
            centerRe = anchorRe - (focusX - width / 2.0) * newScale,
            centerIm = anchorIm + (focusY - height / 2.0) * newScale,
            scale = newScale,
        ).clamped()
    }

    /** The same centre and scale in an image of another size (rotation, split-screen), clamped with [clamped]. */
    fun resizedTo(width: Int, height: Int): Viewport = copy(width = width, height = height).clamped()

    /**
     * This viewport with its scale limited to [MIN_SCALE]..(scale of [fitWholeSet] for this size)
     * and its centre limited to the rectangle that contains the set.
     */
    fun clamped(): Viewport = copy(
        centerRe = centerRe.coerceIn(SET_MIN_RE, SET_MAX_RE),
        centerIm = centerIm.coerceIn(SET_MIN_IM, SET_MAX_IM),
        scale = scale.coerceIn(MIN_SCALE, maxScale()),
    )

    /** Where an image rendered for [source] must be drawn so that it lines up with this viewport. */
    fun placementOf(source: Viewport): ImagePlacement {
        val scaleFactor = source.scale / scale
        return ImagePlacement(
            left = width / 2.0 + (source.centerRe - centerRe) / scale - source.width / 2.0 * scaleFactor,
            top = height / 2.0 - (source.centerIm - centerIm) / scale - source.height / 2.0 * scaleFactor,
            scaleFactor = scaleFactor,
        )
    }

    /** The scale at which the whole set just fits: the zoom-out limit for this image size. */
    private fun maxScale(): Double = fitWholeSet(width, height).scale

    companion object {
        const val SET_MIN_RE = -2.0
        const val SET_MAX_RE = 0.5
        const val SET_MIN_IM = -1.25
        const val SET_MAX_IM = 1.25

        /** Deepest zoom: below this scale, neighbouring pixels are too close for Double arithmetic. */
        const val MIN_SCALE = 1e-13

        /** Margin left on each side of the set, as a fraction of the set's size. */
        private const val MARGIN = 0.05

        /** Offset from a pixel's left or top edge to its centre. */
        private const val PIXEL_CENTER = 0.5

        /**
         * The viewport that shows the whole set plus a margin, centred, in an image of the given size.
         * Uses the larger of the two per-axis scales, so the set fits in both portrait and landscape.
         */
        fun fitWholeSet(width: Int, height: Int): Viewport {
            val regionWidth = (SET_MAX_RE - SET_MIN_RE) * (1 + 2 * MARGIN)
            val regionHeight = (SET_MAX_IM - SET_MIN_IM) * (1 + 2 * MARGIN)
            return Viewport(
                centerRe = (SET_MIN_RE + SET_MAX_RE) / 2,
                centerIm = (SET_MIN_IM + SET_MAX_IM) / 2,
                scale = maxOf(regionWidth / width, regionHeight / height),
                width = width,
                height = height,
            )
        }
    }
}

/** Position of an image's top-left corner in pixels of the target viewport, and its magnification. */
data class ImagePlacement(val left: Double, val top: Double, val scaleFactor: Double)
