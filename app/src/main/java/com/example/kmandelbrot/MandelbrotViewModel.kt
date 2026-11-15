package com.example.kmandelbrot

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.createBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MandelbrotViewModel : ViewModel() {

    /** The image being drawn; null until the first size is known. */
    var image: ImageBitmap? by mutableStateOf(null)
        private set

    /** Incremented each time a band is written into [image], to trigger a redraw. */
    var frame: Int by mutableIntStateOf(0)
        private set

    private var renderJob: Job? = null

    /**
     * Called with the canvas size in pixels. Does nothing if the size is unchanged
     * (so a configuration change that keeps the size does not re-render).
     * Otherwise cancels the running render, creates a transparent bitmap of the new size,
     * and starts a new render.
     */
    fun onSizeChanged(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        val current = image
        if (current != null && current.width == width && current.height == height) return

        renderJob?.cancel()
        val bitmap = createBitmap(width, height)
        image = bitmap.asImageBitmap()

        val viewport = Viewport.fitWholeSet(width, height)
        renderJob = viewModelScope.launch {
            renderMandelbrot(viewport) { firstRow, pixels ->
                // Write on the main thread so the bitmap is never modified while it is being drawn.
                withContext(Dispatchers.Main) {
                    bitmap.setPixels(pixels, 0, width, 0, firstRow, width, pixels.size / width)
                    frame++
                }
            }
        }
    }
}
