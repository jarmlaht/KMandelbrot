package com.example.kmandelbrot

/** Smallest fixed iteration limit: the colour table needs at least two iterations. */
const val MIN_FIXED_ITERATION_LIMIT = 2

/** Largest fixed iteration limit: keeps the render time and the colour table within reason. */
const val MAX_FIXED_ITERATION_LIMIT = 100_000

/** How the iteration limit of an image is chosen. */
enum class IterationMode {
    /** The limit grows with zoom depth, see [iterationLimitForScale]. */
    SCALE_BASED,

    /** The limit is the same at every zoom depth. */
    FIXED,
}

/** The user's iteration settings. [fixedLimit] is used only in [IterationMode.FIXED]. */
data class IterationSettings(
    val mode: IterationMode = IterationMode.SCALE_BASED,
    val fixedLimit: Int = BASE_ITERATION_LIMIT,
) {
    init {
        require(fixedLimit in MIN_FIXED_ITERATION_LIMIT..MAX_FIXED_ITERATION_LIMIT) {
            "fixedLimit must be in $MIN_FIXED_ITERATION_LIMIT..$MAX_FIXED_ITERATION_LIMIT, was $fixedLimit"
        }
    }

    /** The iteration limit for an image at [scale] (complex units per pixel). */
    fun limitFor(scale: Double): Int = when (mode) {
        IterationMode.SCALE_BASED -> iterationLimitForScale(scale)
        IterationMode.FIXED -> fixedLimit
    }
}

/**
 * The number in [text] if it is a whole number in
 * [MIN_FIXED_ITERATION_LIMIT]..[MAX_FIXED_ITERATION_LIMIT], otherwise null.
 */
fun parseFixedIterationLimit(text: String): Int? =
    text.toIntOrNull()?.takeIf { it in MIN_FIXED_ITERATION_LIMIT..MAX_FIXED_ITERATION_LIMIT }
