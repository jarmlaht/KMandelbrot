package com.example.kmandelbrot

import java.util.Locale

/**
 * [value] as plain decimal text with 16 decimals, e.g. -0.7436438870371587: enough to tell views apart
 * at the deepest zoom. Never uses exponent notation, unlike `Double.toString()` for small values.
 */
fun formatCoordinate(value: Double): String = String.format(Locale.ROOT, "%.16f", value)
