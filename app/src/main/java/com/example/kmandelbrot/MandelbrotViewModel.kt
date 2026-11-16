package com.example.kmandelbrot

import android.graphics.Bitmap
import android.graphics.Paint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MandelbrotViewModel(private val savedState: SavedStateHandle) : ViewModel() {

    /** The bitmap on screen. Null until the first size is known. */
    var image: ImageBitmap? by mutableStateOf(null)
        private set

    /** The viewport [image]'s content was made for. */
    var imageViewport: Viewport? by mutableStateOf(null)
        private set

    /** The view the user is looking at. Follows the fingers during a gesture. */
    var viewport: Viewport? by mutableStateOf(null)
        private set

    /** Incremented each time a band is written into [image], to trigger a redraw. */
    var frame: Int by mutableIntStateOf(0)
        private set

    private var bitmap: Bitmap? = null
    private var renderJob: Job? = null

    /** True when [viewport] has changed since the last render was started. */
    private var renderNeeded = false

    private val previewPaint = Paint(Paint.FILTER_BITMAP_FLAG)

    /**
     * Called with the canvas size in pixels. The first call restores the saved view, or fits the whole set.
     * A later call with a new size keeps the centre and scale. Does nothing if the size is unchanged
     * (so a configuration change that keeps the size does not re-render).
     */
    fun onSizeChanged(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        val current = viewport
        if (current != null && current.width == width && current.height == height) return

        viewport = current?.resizedTo(width, height)
            ?: restoredViewport(width, height)
            ?: Viewport.fitWholeSet(width, height)
        startRender()
    }

    /** One step of a pinch/drag gesture, in pixels. Updates [viewport]; stops a running render if the view changed. */
    fun onTransform(focusX: Float, focusY: Float, panX: Float, panY: Float, zoom: Float) {
        val current = viewport ?: return
        if (!(zoom > 0f)) return
        val transformed = current.transformedBy(
            focusX = focusX.toDouble(),
            focusY = focusY.toDouble(),
            panX = panX.toDouble(),
            panY = panY.toDouble(),
            zoom = zoom.toDouble(),
        )
        if (transformed == current) return

        renderJob?.cancel()
        renderNeeded = true
        viewport = transformed
    }

    /** All fingers lifted: recalculate the image if the view changed during the gesture. */
    fun onGestureEnd() {
        if (renderNeeded) startRender()
    }

    /**
     * Replaces [image] with a preview of [viewport] made from the previous image, then renders the sharp
     * image into it band by band.
     */
    private fun startRender() {
        val target = viewport ?: return
        renderJob?.cancel()
        renderNeeded = false

        val newBitmap = createBitmap(target.width, target.height)
        val previousBitmap = bitmap
        val previousViewport = imageViewport
        if (previousBitmap != null && previousViewport != null) {
            val placement = target.placementOf(previousViewport)
            val scaleFactor = placement.scaleFactor.toFloat()
            newBitmap.applyCanvas {
                translate(placement.left.toFloat(), placement.top.toFloat())
                scale(scaleFactor, scaleFactor)
                drawBitmap(previousBitmap, 0f, 0f, previewPaint)
            }
        }
        bitmap = newBitmap
        image = newBitmap.asImageBitmap()
        imageViewport = target
        saveViewport(target)

        val width = target.width
        renderJob = viewModelScope.launch {
            renderMandelbrot(target, iterationLimitForScale(target.scale)) { firstRow, pixels ->
                // Write on the main thread so the bitmap is never modified while it is being drawn.
                withContext(Dispatchers.Main) {
                    newBitmap.setPixels(pixels, 0, width, 0, firstRow, width, pixels.size / width)
                    frame++
                }
            }
        }
    }

    private fun saveViewport(viewport: Viewport) {
        savedState[KEY_CENTER_RE] = viewport.centerRe
        savedState[KEY_CENTER_IM] = viewport.centerIm
        savedState[KEY_SCALE] = viewport.scale
    }

    private fun restoredViewport(width: Int, height: Int): Viewport? {
        val centerRe = savedState.get<Double>(KEY_CENTER_RE) ?: return null
        val centerIm = savedState.get<Double>(KEY_CENTER_IM) ?: return null
        val scale = savedState.get<Double>(KEY_SCALE) ?: return null
        return Viewport(centerRe, centerIm, scale, width, height).clamped()
    }

    private companion object {
        const val KEY_CENTER_RE = "centerRe"
        const val KEY_CENTER_IM = "centerIm"
        const val KEY_SCALE = "scale"
    }
}
