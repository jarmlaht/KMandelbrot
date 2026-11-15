package com.example.kmandelbrot

import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

const val COLOR_INSIDE = 0xFF000000.toInt() // black, ARGB
const val COLOR_OUTSIDE = 0xFFFFFFFF.toInt() // white, ARGB

/**
 * Calculates the image for [viewport] in bands of [bandRows] rows.
 * One worker coroutine per CPU core runs on Dispatchers.Default; each takes the next
 * unclaimed band from a shared counter, so bands complete roughly top to bottom.
 * [onBand] is called once per band with its first row and its pixels (row-major, ARGB).
 * Cancelling the calling coroutine stops the workers.
 */
suspend fun renderMandelbrot(
    viewport: Viewport,
    bandRows: Int = 16,
    onBand: suspend (firstRow: Int, pixels: IntArray) -> Unit,
) {
    val width = viewport.width
    val height = viewport.height
    val bandCount = (height + bandRows - 1) / bandRows
    val nextBand = AtomicInteger(0)
    val workerCount = minOf(Runtime.getRuntime().availableProcessors(), bandCount)

    coroutineScope {
        repeat(workerCount) {
            launch(Dispatchers.Default) {
                while (true) {
                    val band = nextBand.getAndIncrement()
                    if (band >= bandCount) break
                    val firstRow = band * bandRows
                    val rows = minOf(bandRows, height - firstRow)
                    val pixels = IntArray(rows * width)
                    for (row in 0 until rows) {
                        val im = viewport.im(firstRow + row)
                        val offset = row * width
                        for (x in 0 until width) {
                            pixels[offset + x] =
                                if (isInMandelbrotSet(viewport.re(x), im)) COLOR_INSIDE else COLOR_OUTSIDE
                        }
                    }
                    onBand(firstRow, pixels)
                }
            }
        }
    }
}
