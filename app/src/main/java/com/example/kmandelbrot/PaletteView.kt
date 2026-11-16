package com.example.kmandelbrot

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

private val PALETTE_ITEM_SPACING = 16.dp

private val BAR_HEIGHT = 40.dp
private val BAR_CORNER_RADIUS = 4.dp
private val THUMB_WIDTH = 16.dp
private val THUMB_BORDER_WIDTH = 2.dp

private val COLOR_BOX_SIZE = 72.dp
private val BORDER_WIDTH = 1.dp
private val SELECTED_BORDER_WIDTH = 3.dp

/** The gradient box fills this fraction of the Palette view's width. */
private const val GRADIENT_WIDTH_FRACTION = 0.9f

/** Number of evenly spaced hues along the hue bar: one per sector of the colour wheel, plus the end. */
private const val HUE_STOP_COUNT = 7

/** The colours along the hue bar: red, yellow, green, cyan, blue, magenta, red. */
private val HUE_BAR_COLORS: List<Color> = List(HUE_STOP_COUNT) { stop ->
    Color(pickerColor(hue = MAX_HUE * stop / (HUE_STOP_COUNT - 1), tone = PURE_COLOR_TONE))
}

/**
 * The content of the Palette tab: the instruction, the hue and brightness bars, the two colour boxes, the
 * gradient between them and the reset button. [hue] and [tone] are the position of the bars; the box chosen
 * by [edgeSelected] is the one they edit. Colours are opaque ARGB.
 */
@Composable
fun PaletteView(
    farColor: Int,
    edgeColor: Int,
    edgeSelected: Boolean,
    hue: Float,
    tone: Float,
    onHueChange: (Float) -> Unit,
    onToneChange: (Float) -> Unit,
    onBoxSelected: (edge: Boolean) -> Unit,
    onReset: () -> Unit,
) {
    val pureHue = Color(pickerColor(hue, PURE_COLOR_TONE))
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PALETTE_ITEM_SPACING),
    ) {
        Text(stringResource(R.string.palette_instruction), style = MaterialTheme.typography.titleMedium)
        ColorBar(
            colors = HUE_BAR_COLORS,
            fraction = hue / MAX_HUE,
            thumbColor = pureHue,
            description = stringResource(R.string.palette_hue),
            onFractionChange = { onHueChange(it * MAX_HUE) },
        )
        ColorBar(
            colors = listOf(Color.Black, pureHue, Color.White),
            fraction = tone,
            thumbColor = Color(pickerColor(hue, tone)),
            description = stringResource(R.string.palette_brightness),
            onFractionChange = onToneChange,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            ColorBox(
                color = farColor,
                selected = !edgeSelected,
                description = stringResource(R.string.palette_far_color),
                onClick = { onBoxSelected(false) },
            )
            ColorBox(
                color = edgeColor,
                selected = edgeSelected,
                description = stringResource(R.string.palette_edge_color),
                onClick = { onBoxSelected(true) },
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(GRADIENT_WIDTH_FRACTION)
                .height(BAR_HEIGHT)
                .background(Brush.horizontalGradient(listOf(Color(farColor), Color(edgeColor))))
                .border(BORDER_WIDTH, Color.LightGray),
        )
        TextButton(onClick = onReset, modifier = Modifier.align(Alignment.End)) {
            Text(stringResource(R.string.palette_reset))
        }
    }
}

/**
 * A horizontal bar filled with [colors] left to right, with a thumb at [fraction] (0..1) of its width.
 * Touching the bar moves the thumb under the finger, and it follows the finger while dragged.
 */
@Composable
private fun ColorBar(
    colors: List<Color>,
    fraction: Float,
    thumbColor: Color,
    description: String,
    onFractionChange: (Float) -> Unit,
) {
    val currentOnFractionChange by rememberUpdatedState(onFractionChange)
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(BAR_HEIGHT)
            .semantics { contentDescription = description }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    currentOnFractionChange((down.position.x / size.width).coerceIn(0f, 1f))
                    horizontalDrag(down.id) { change ->
                        change.consume()
                        currentOnFractionChange((change.position.x / size.width).coerceIn(0f, 1f))
                    }
                }
            },
    ) {
        drawRoundRect(
            brush = Brush.horizontalGradient(colors),
            cornerRadius = CornerRadius(BAR_CORNER_RADIUS.toPx()),
        )
        // The thumb is centred on its position but never sticks out of the bar.
        val thumbWidth = THUMB_WIDTH.toPx()
        val thumbLeft = (fraction * size.width - thumbWidth / 2).coerceIn(0f, size.width - thumbWidth)
        val thumbTopLeft = Offset(thumbLeft, 0f)
        val thumbSize = Size(thumbWidth, size.height)
        val thumbCornerRadius = CornerRadius(BAR_CORNER_RADIUS.toPx())
        drawRoundRect(thumbColor, thumbTopLeft, thumbSize, thumbCornerRadius)
        drawRoundRect(
            color = Color.White,
            topLeft = thumbTopLeft,
            size = thumbSize,
            cornerRadius = thumbCornerRadius,
            style = Stroke(THUMB_BORDER_WIDTH.toPx()),
        )
    }
}

/** A square filled with [color] that selects itself when tapped; the selected box has a thicker border. */
@Composable
private fun ColorBox(color: Int, selected: Boolean, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(COLOR_BOX_SIZE)
            .semantics { contentDescription = description }
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .background(Color(color))
            .border(
                width = if (selected) SELECTED_BORDER_WIDTH else BORDER_WIDTH,
                color = if (selected) Color.White else Color.LightGray,
            ),
    )
}
