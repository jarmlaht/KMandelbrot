package com.example.kmandelbrot

const val MAX_ITERATIONS = 256

/** Returned by [escapeIterations] for a point that did not escape, i.e. is treated as inside the set. */
const val NOT_ESCAPED = 0

/** |z| > 2 means the point escapes; compared as |z|² > 4 to avoid a square root. */
private const val ESCAPE_RADIUS_SQUARED = 4.0

/**
 * Iterates z = z² + c from z = 0 for c = cRe + cIm·i.
 * Returns the number of iterations (1..[maxIterations]) after which |z| first exceeds 2,
 * or [NOT_ESCAPED] if it never does within [maxIterations].
 */
fun escapeIterations(cRe: Double, cIm: Double, maxIterations: Int = MAX_ITERATIONS): Int {
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
