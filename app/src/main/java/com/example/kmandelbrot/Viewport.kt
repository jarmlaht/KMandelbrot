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
    fun re(x: Int): Double = centerRe + (x + 0.5 - width / 2.0) * scale

    /** Imaginary coordinate of the centre of pixel row [y]; row 0 is the top, so im decreases as y grows. */
    fun im(y: Int): Double = centerIm - (y + 0.5 - height / 2.0) * scale

    companion object {
        const val SET_MIN_RE = -2.0
        const val SET_MAX_RE = 0.5
        const val SET_MIN_IM = -1.25
        const val SET_MAX_IM = 1.25

        /** Margin left on each side of the set, as a fraction of the set's size. */
        private const val MARGIN = 0.05

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
