package com.example.kmandelbrot

import kotlin.math.ln
import kotlin.math.roundToInt

const val PALETTE_SIZE = 256

const val COLOR_IN_SET = 0xFF000000.toInt() // black, ARGB

const val COLOR_RED = 0xFFFF0000.toInt() // ARGB
const val COLOR_YELLOW = 0xFFFFFF00.toInt() // ARGB

private const val OPAQUE_ALPHA = 0xFF000000.toInt()
private const val RED_SHIFT = 16
private const val GREEN_SHIFT = 8
private const val BLUE_SHIFT = 0
private const val CHANNEL_MAX = 0xFF

/** The two colours the palette is built from, as opaque ARGB. */
data class PaletteColors(
    /** Colour of points far from the set (escape on the first iteration). */
    val farColor: Int = COLOR_RED,
    /** Colour of points at the edge of the set (escape on the last iteration). */
    val edgeColor: Int = COLOR_YELLOW,
)

/**
 * The gradient of [PALETTE_SIZE] opaque ARGB colours from [startColor] (index 0) to [endColor] (last index).
 * Red, green and blue each change evenly, rounded to the nearest value.
 */
fun colorGradient(startColor: Int, endColor: Int): IntArray = IntArray(PALETTE_SIZE) { index ->
    val red = blendedChannel(startColor, endColor, RED_SHIFT, index)
    val green = blendedChannel(startColor, endColor, GREEN_SHIFT, index)
    val blue = blendedChannel(startColor, endColor, BLUE_SHIFT, index)
    OPAQUE_ALPHA or (red shl RED_SHIFT) or (green shl GREEN_SHIFT) or (blue shl BLUE_SHIFT)
}

/** The colour channel at [shift] of the gradient colour at [index]. */
private fun blendedChannel(startColor: Int, endColor: Int, shift: Int, index: Int): Int {
    val start = (startColor shr shift) and CHANNEL_MAX
    val end = (endColor shr shift) and CHANNEL_MAX
    val lastIndex = PALETTE_SIZE - 1
    // Adding half the divisor rounds to the nearest value instead of down.
    return (start * (lastIndex - index) + end * index + lastIndex / 2) / lastIndex
}

/**
 * Lookup table from the result of [escapeIterations] to an ARGB colour, of size [maxIterations] + 1.
 * Index [NOT_ESCAPED] is [COLOR_IN_SET]. Index n (1..[maxIterations]) is the colour at position
 * ln(n) / ln(maxIterations) along the gradient of [palette], so 1 iteration has its far colour and
 * [maxIterations] iterations its edge colour.
 */
fun iterationColors(
    maxIterations: Int = BASE_ITERATION_LIMIT,
    palette: PaletteColors = PaletteColors(),
): IntArray {
    require(maxIterations >= 2) { "maxIterations must be at least 2, was $maxIterations" }
    val gradient = colorGradient(palette.farColor, palette.edgeColor)
    val logMaxIterations = ln(maxIterations.toDouble())
    return IntArray(maxIterations + 1) { iterations ->
        if (iterations == NOT_ESCAPED) {
            COLOR_IN_SET
        } else {
            val position = ln(iterations.toDouble()) / logMaxIterations
            gradient[(gradient.lastIndex * position).roundToInt()]
        }
    }
}
