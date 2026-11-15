package com.example.kmandelbrot

const val MAX_ITERATIONS = 256

/** True if c = cRe + cIm·i does not escape (|z| > 2) within [maxIterations] of z = z² + c. */
fun isInMandelbrotSet(cRe: Double, cIm: Double, maxIterations: Int = MAX_ITERATIONS): Boolean {
    var re = 0.0
    var im = 0.0
    for (i in 0 until maxIterations) {
        val re2 = re * re
        val im2 = im * im
        if (re2 + im2 > 4.0) return false
        im = 2.0 * re * im + cIm
        re = re2 - im2 + cRe
    }
    return true
}
