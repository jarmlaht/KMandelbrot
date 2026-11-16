package com.example.kmandelbrot

import kotlin.math.log10
import kotlin.math.roundToInt

/** Iteration limit at the start view. */
const val BASE_ITERATION_LIMIT = 256

/** The iteration limit never grows beyond this. */
const val ITERATION_LIMIT_CAP = 10_000

/** Returned by [escapeIterations] for a point that did not escape, i.e. is treated as inside the set. */
const val NOT_ESCAPED = 0

/** Scale (complex units per pixel) of about the start view on a phone; the limit starts growing below it. */
private const val REFERENCE_SCALE = 0.0025

/** How much the iteration limit grows for every tenfold magnification. */
private const val ITERATIONS_PER_DECADE = 940

/** |z| > 2 means the point escapes; compared as |z|² > 4 to avoid a square root. */
private const val ESCAPE_RADIUS_SQUARED = 4.0

/** Real coordinate of the cusp of the main cardioid. */
private const val CARDIOID_CUSP_RE = 0.25

/** The period-2 bulb is the disc of radius 1/4 centred on -1. */
private const val PERIOD_2_BULB_CENTER_RE = -1.0
private const val PERIOD_2_BULB_RADIUS_SQUARED = 0.0625

/**
 * The iteration limit to use at [scale] (complex units per pixel): [BASE_ITERATION_LIMIT] at
 * [REFERENCE_SCALE] and above, plus [ITERATIONS_PER_DECADE] for every tenfold magnification beyond it,
 * capped at [ITERATION_LIMIT_CAP].
 */
fun iterationLimitForScale(scale: Double): Int {
    require(scale > 0.0) { "scale must be positive, was $scale" }
    val decades = log10(REFERENCE_SCALE / scale)
    val limit = BASE_ITERATION_LIMIT + (ITERATIONS_PER_DECADE * decades).roundToInt()
    return limit.coerceIn(BASE_ITERATION_LIMIT, ITERATION_LIMIT_CAP)
}

/**
 * Iterates z = z² + c from z = 0 for c = cRe + cIm·i.
 * Returns the number of iterations (1..[maxIterations]) after which |z| first exceeds 2,
 * or [NOT_ESCAPED] if it never does within [maxIterations].
 */
fun escapeIterations(cRe: Double, cIm: Double, maxIterations: Int = BASE_ITERATION_LIMIT): Int {
    if (isInMainCardioidOrPeriod2Bulb(cRe, cIm)) return NOT_ESCAPED
    var re = 0.0
    var im = 0.0
    for (iteration in 1..maxIterations) {
        val nextRe = re * re - im * im + cRe
        im = 2.0 * re * im + cIm
        re = nextRe
        if (re * re + im * im > ESCAPE_RADIUS_SQUARED) return iteration
    }
    return NOT_ESCAPED
}

/**
 * True for points in the two largest parts of the set, which are known never to escape.
 * Skipping the iteration for them does not change any result; it only saves time.
 */
private fun isInMainCardioidOrPeriod2Bulb(cRe: Double, cIm: Double): Boolean {
    val imSquared = cIm * cIm
    val x = cRe - CARDIOID_CUSP_RE
    val q = x * x + imSquared
    if (q * (q + x) <= imSquared / 4) return true
    val bulbX = cRe - PERIOD_2_BULB_CENTER_RE
    return bulbX * bulbX + imSquared <= PERIOD_2_BULB_RADIUS_SQUARED
}
