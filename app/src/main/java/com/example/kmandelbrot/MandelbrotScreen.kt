package com.example.kmandelbrot

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MandelbrotScreen(viewModel: MandelbrotViewModel = viewModel()) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .onSizeChanged { viewModel.onSizeChanged(it.width, it.height) }
    ) {
        viewModel.frame // read so each new band redraws the canvas
        viewModel.image?.let { drawImage(it) }
    }
}
