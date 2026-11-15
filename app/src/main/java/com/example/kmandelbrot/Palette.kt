package com.example.kmandelbrot

import kotlin.math.ln
import kotlin.math.roundToInt

const val PALETTE_SIZE = 256

const val COLOR_IN_SET = 0xFF000000.toInt() // black, ARGB

private const val COLOR_RED = 0xFFFF0000.toInt() // ARGB
private const val GREEN_SHIFT = 8

/**
 * The gradient of [PALETTE_SIZE] opaque ARGB colours from red (#FF0000, index 0) to yellow (#FFFF00, last index).
 * Red is always FF and blue always 00; green rises from 00 to FF, one step per colour.
 */
fun redToYellowGradient(): IntArray = IntArray(PALETTE_SIZE) { green -> COLOR_RED or (green shl GREEN_SHIFT) }

/**
 * Lookup table from the result of [escapeIterations] to an ARGB colour, of size [maxIterations] + 1.
 * Index [NOT_ESCAPED] is [COLOR_IN_SET]. Index n (1..[maxIterations]) is the gradient colour at
 * position ln(n) / ln(maxIterations) along the gradient, so 1 iteration is red and
 * [maxIterations] iterations is yellow.
 */
fun iterationColors(maxIterations: Int = MAX_ITERATIONS): IntArray {
    require(maxIterations >= 2) { "maxIterations must be at least 2, was $maxIterations" }
    val gradient = redToYellowGradient()
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
