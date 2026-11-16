package com.example.kmandelbrot

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MandelbrotScreen(
    viewModel: MandelbrotViewModel = viewModel { MandelbrotViewModel(createSavedStateHandle()) },
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onSizeChanged { viewModel.onSizeChanged(it.width, it.height) }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    viewModel.onTransform(centroid.x, centroid.y, pan.x, pan.y, zoom)
                }
            }
            .pointerInput(Unit) {
                // detectTransformGestures has no end callback, so watch for all fingers lifting.
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    try {
                        do {
                            val event = awaitPointerEvent(PointerEventPass.Final)
                        } while (event.changes.any { it.pressed })
                    } finally {
                        // Also runs when the system takes the gesture over (e.g. an edge swipe),
                        // so a view moved by a cancelled gesture is still recalculated.
                        viewModel.onGestureEnd()
                    }
                }
            }
    ) {
        viewModel.frame // read so each new band redraws the canvas
        val image = viewModel.image
        val imageViewport = viewModel.imageViewport
        val viewport = viewModel.viewport
        if (image != null && imageViewport != null && viewport != null) {
            // During a gesture the image was made for another viewport: move and scale it to line up.
            val placement = viewport.placementOf(imageViewport)
            val scaleFactor = placement.scaleFactor.toFloat()
            withTransform({
                translate(placement.left.toFloat(), placement.top.toFloat())
                scale(scaleFactor, scaleFactor, Offset.Zero)
            }) {
                drawImage(image)
            }
        }
    }
}
