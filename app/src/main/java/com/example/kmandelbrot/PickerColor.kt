package com.example.kmandelbrot

import kotlin.math.abs
import kotlin.math.roundToInt

const val MAX_HUE = 360f

/** Tone of a pure, fully saturated colour: halfway between black (0) and white (1). */
const val PURE_COLOR_TONE = 0.5f

/** The colour wheel has six sectors of this many degrees: red, yellow, green, cyan, blue, magenta. */
private const val HUE_SECTOR_DEGREES = 60f

private const val OPAQUE = 0xFF000000.toInt()
private const val RED_SHIFT = 16
private const val GREEN_SHIFT = 8
private const val CHANNEL_MAX = 0xFF

/** A position of the colour picker: [hue] in degrees (0..360) and [tone] from 0 (black) to 1 (white). */
data class PickerPosition(val hue: Float, val tone: Float)

/**
 * The opaque ARGB colour at [hue] and [tone]: black at tone 0, the pure hue at tone 0.5, white at tone 1,
 * blended evenly in between. This is the HSL colour model at full saturation.
 */
fun pickerColor(hue: Float, tone: Float): Int {
    val lightness = tone.coerceIn(0f, 1f)
    val chroma = 1f - abs(2f * lightness - 1f)
    val sector = (hue.coerceIn(0f, MAX_HUE) % MAX_HUE) / HUE_SECTOR_DEGREES
    val middle = chroma * (1f - abs(sector % 2f - 1f))
    val offset = lightness - chroma / 2f
    val (red, green, blue) = when (sector.toInt()) {
        0 -> Triple(chroma, middle, 0f)
        1 -> Triple(middle, chroma, 0f)
        2 -> Triple(0f, chroma, middle)
        3 -> Triple(0f, middle, chroma)
        4 -> Triple(middle, 0f, chroma)
        else -> Triple(chroma, 0f, middle)
    }
    return OPAQUE or
        (channel(red + offset) shl RED_SHIFT) or
        (channel(green + offset) shl GREEN_SHIFT) or
        channel(blue + offset)
}

/** The picker position that gives [color], or the nearest one. Black, white and greys get hue 0. */
fun pickerPositionOf(color: Int): PickerPosition {
    val red = ((color shr RED_SHIFT) and CHANNEL_MAX) / CHANNEL_MAX.toFloat()
    val green = ((color shr GREEN_SHIFT) and CHANNEL_MAX) / CHANNEL_MAX.toFloat()
    val blue = (color and CHANNEL_MAX) / CHANNEL_MAX.toFloat()
    val max = maxOf(red, green, blue)
    val min = minOf(red, green, blue)
    val chroma = max - min
    val sector = when {
        chroma == 0f -> 0f
        max == red -> ((green - blue) / chroma).mod(6f)
        max == green -> (blue - red) / chroma + 2f
        else -> (red - green) / chroma + 4f
    }
    return PickerPosition(hue = sector * HUE_SECTOR_DEGREES, tone = (max + min) / 2f)
}

/** A colour channel from 0..1 as 0..255. */
private fun channel(value: Float): Int = (value * CHANNEL_MAX).roundToInt().coerceIn(0, CHANNEL_MAX)
