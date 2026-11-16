package com.example.kmandelbrot

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/** The view fills this fraction of the screen's width and height. */
private const val PANEL_SCREEN_FRACTION = 0.9f

private const val PANEL_BACKGROUND_ALPHA = 0.8f
private const val MENU_BUTTON_BACKGROUND_ALPHA = 0.5f

private val PANEL_BORDER_WIDTH = 1.dp
private val PANEL_CORNER_RADIUS = 8.dp
private val CONTENT_PADDING = 16.dp
private val SECTION_SPACING = 24.dp
private val ITEM_SPACING = 8.dp
private val RADIO_ROW_MIN_HEIGHT = 48.dp

private val ICON_SIZE = 24.dp
private val ICON_STROKE_WIDTH = 2.dp

/** Distance of an icon's lines from the edge of the icon, as a fraction of its size. */
private const val ICON_INSET_FRACTION = 0.2f

private enum class PanelTab(@param:StringRes val titleRes: Int) {
    INFO(R.string.tab_info),
    PALETTE(R.string.tab_palette),
    SETTINGS(R.string.tab_settings),
}

/** The button that opens the info/settings view: a three-line icon where the view's 'X' will be. */
@Composable
fun MenuButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.open_menu)
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.fillMaxSize(PANEL_SCREEN_FRACTION), contentAlignment = Alignment.TopEnd) {
            IconButton(
                onClick = onClick,
                modifier = Modifier.semantics { contentDescription = description },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Black.copy(alpha = MENU_BUTTON_BACKGROUND_ALPHA),
                    contentColor = Color.White,
                ),
            ) {
                MenuIcon()
            }
        }
    }
}

/**
 * The info/settings view with the area around it, covering the whole screen. [onClose] is called with the
 * settings to apply when the 'X' or the area outside the view is tapped.
 * [centerRe], [centerIm] and [iterationLimit] describe the image on screen; [settings] are those in use.
 */
@Composable
fun InfoSettingsPanel(
    centerRe: Double?,
    centerIm: Double?,
    iterationLimit: Int?,
    settings: IterationSettings,
    onClose: (IterationSettings) -> Unit,
) {
    var selectedTab by rememberSaveable { mutableStateOf(PanelTab.INFO) }
    var limitText by rememberSaveable { mutableStateOf(settings.fixedLimit.toString()) }
    var mode by rememberSaveable { mutableStateOf(settings.mode) }
    val close = {
        // An empty or out-of-range field keeps the previous value.
        onClose(IterationSettings(mode, parseFixedIterationLimit(limitText) ?: settings.fixedLimit))
    }

    // Always the dark colours, so the view looks the same in the light and dark system themes.
    MaterialTheme(colorScheme = darkColorScheme()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(interactionSource = null, indication = null, onClick = close),
            )
            Surface(
                modifier = Modifier.fillMaxSize(PANEL_SCREEN_FRACTION),
                shape = RoundedCornerShape(PANEL_CORNER_RADIUS),
                color = Color.Black.copy(alpha = PANEL_BACKGROUND_ALPHA),
                contentColor = Color.White,
                border = BorderStroke(PANEL_BORDER_WIDTH, Color.LightGray),
            ) {
                Column {
                    MenuBar(selectedTab = selectedTab, onTabSelected = { selectedTab = it }, onClose = close)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(CONTENT_PADDING),
                    ) {
                        when (selectedTab) {
                            PanelTab.INFO -> InfoView(centerRe, centerIm, iterationLimit)
                            PanelTab.PALETTE -> Unit // Defined in a separate issue.
                            PanelTab.SETTINGS -> SettingsView(
                                limitText = limitText,
                                onLimitTextChange = { limitText = it },
                                mode = mode,
                                onModeChange = { mode = it },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuBar(selectedTab: PanelTab, onTabSelected: (PanelTab) -> Unit, onClose: () -> Unit) {
    val closeDescription = stringResource(R.string.close_menu)
    Row(verticalAlignment = Alignment.CenterVertically) {
        PrimaryTabRow(
            selectedTabIndex = selectedTab.ordinal,
            modifier = Modifier.weight(1f),
            containerColor = Color.Transparent,
        ) {
            for (tab in PanelTab.entries) {
                Tab(
                    selected = tab == selectedTab,
                    onClick = { onTabSelected(tab) },
                    text = { Text(stringResource(tab.titleRes), maxLines = 1) },
                )
            }
        }
        IconButton(
            onClick = onClose,
            modifier = Modifier.semantics { contentDescription = closeDescription },
        ) {
            CloseIcon()
        }
    }
}

@Composable
private fun InfoView(centerRe: Double?, centerIm: Double?, iterationLimit: Int?) {
    Text(stringResource(R.string.image_center_coordinates), style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(ITEM_SPACING))
    Text(stringResource(R.string.coordinate_x, centerRe?.let(::formatCoordinate).orEmpty()))
    Text(stringResource(R.string.coordinate_y, centerIm?.let(::formatCoordinate).orEmpty()))
    Spacer(Modifier.height(SECTION_SPACING))
    Text(stringResource(R.string.current_iteration_count), style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(ITEM_SPACING))
    Text(iterationLimit?.toString().orEmpty())
}

@Composable
private fun SettingsView(
    limitText: String,
    onLimitTextChange: (String) -> Unit,
    mode: IterationMode,
    onModeChange: (IterationMode) -> Unit,
) {
    Text(stringResource(R.string.maximum_iteration), style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(ITEM_SPACING))
    OutlinedTextField(
        value = limitText,
        onValueChange = { text -> onLimitTextChange(text.filter { it in '0'..'9' }) },
        enabled = mode == IterationMode.FIXED,
        isError = parseFixedIterationLimit(limitText) == null,
        supportingText = {
            Text(
                stringResource(
                    R.string.maximum_iteration_range,
                    MIN_FIXED_ITERATION_LIMIT,
                    MAX_FIXED_ITERATION_LIMIT,
                ),
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
    Spacer(Modifier.height(SECTION_SPACING))
    Text(stringResource(R.string.iteration_behaviour), style = MaterialTheme.typography.titleMedium)
    Column(modifier = Modifier.selectableGroup()) {
        IterationModeOption(
            label = stringResource(R.string.fixed_iteration_value),
            selected = mode == IterationMode.FIXED,
            onSelect = { onModeChange(IterationMode.FIXED) },
        )
        IterationModeOption(
            label = stringResource(R.string.scale_based_iteration_value),
            selected = mode == IterationMode.SCALE_BASED,
            onSelect = { onModeChange(IterationMode.SCALE_BASED) },
        )
    }
}

@Composable
private fun IterationModeOption(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = RADIO_ROW_MIN_HEIGHT)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect),
        horizontalArrangement = Arrangement.spacedBy(ITEM_SPACING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // The whole row is selectable, so the button itself takes no clicks.
        RadioButton(selected = selected, onClick = null)
        Text(label)
    }
}

/** Three horizontal lines. */
@Composable
private fun MenuIcon() {
    val color = LocalContentColor.current
    Canvas(modifier = Modifier.size(ICON_SIZE)) {
        val inset = size.width * ICON_INSET_FRACTION
        val lineSpacing = (size.height - 2 * inset) / 2
        for (line in 0..2) {
            val y = inset + line * lineSpacing
            drawLine(
                color = color,
                start = Offset(inset, y),
                end = Offset(size.width - inset, y),
                strokeWidth = ICON_STROKE_WIDTH.toPx(),
                cap = StrokeCap.Round,
            )
        }
    }
}

/** An 'X'. */
@Composable
private fun CloseIcon() {
    val color = LocalContentColor.current
    Canvas(modifier = Modifier.size(ICON_SIZE)) {
        val near = size.width * ICON_INSET_FRACTION
        val far = size.width - near
        drawLine(color, Offset(near, near), Offset(far, far), ICON_STROKE_WIDTH.toPx(), StrokeCap.Round)
        drawLine(color, Offset(near, far), Offset(far, near), ICON_STROKE_WIDTH.toPx(), StrokeCap.Round)
    }
}
