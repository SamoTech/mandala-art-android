package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FormatColorReset
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.MandalaPalettes
import com.example.model.Palette
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.PurplePrimary

@Composable
fun ColorPaletteBar(
    selectedColor: Long,
    isEraserActive: Boolean,
    isZoomPanMode: Boolean,
    onColorSelected: (Long) -> Unit,
    onToggleEraser: () -> Unit,
    onToggleZoomPan: () -> Unit,
    onOpenColorPicker: () -> Unit,
    isColorUnlocked: (Long) -> Boolean,
    onRequestUnlockColor: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val palettes = MandalaPalettes.allPalettes
    var selectedPaletteIndex by remember { mutableIntStateOf(0) }
    val currentPalette = palettes[selectedPaletteIndex]

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            // Palette Tabs & Quick Tool Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Category Pills (Scrollable)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    palettes.forEachIndexed { index, palette ->
                        val isSelected = selectedPaletteIndex == index
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedPaletteIndex = index },
                            label = {
                                Text(
                                    text = palette.name,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("palette_tab_${palette.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Custom Color Picker Icon
                IconButton(
                    onClick = onOpenColorPicker,
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("btn_custom_color_picker"),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ColorLens,
                        contentDescription = stringResource(R.string.editor_color_picker),
                        tint = PinkAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Eraser Toggle Button
                IconButton(
                    onClick = onToggleEraser,
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("btn_eraser"),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (isEraserActive) PinkAccent else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isEraserActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatColorReset,
                        contentDescription = stringResource(R.string.editor_eraser),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Zoom / Pan Mode Toggle Button
                IconButton(
                    onClick = onToggleZoomPan,
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("btn_zoom_pan_toggle"),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (isZoomPanMode) PurplePrimary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isZoomPanMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PanTool,
                        contentDescription = stringResource(R.string.editor_zoom),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Color Swatches Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                currentPalette.colors.forEach { colorLong ->
                    val isCurrent = !isEraserActive && selectedColor == colorLong
                    val composeColor = Color(colorLong)

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("color_swatch_${colorLong.toString(16)}")
                            .clip(CircleShape)
                            .clickable {
                                if (isColorUnlocked(colorLong)) onColorSelected(colorLong)
                                else onRequestUnlockColor(colorLong)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Outer selection ring
                        if (isCurrent) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .border(3.dp, PinkAccent, CircleShape)
                            )
                        }

                        // Inner Color Circle
                        Box(
                            modifier = Modifier
                                .size(if (isCurrent) 36.dp else 42.dp)
                                .clip(CircleShape)
                                .background(composeColor)
                                .border(1.dp, Color(0x33000000), CircleShape)
                        )
                        if (!isColorUnlocked(colorLong)) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = "Locked color", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
