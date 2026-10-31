package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.PinkAccent

@Composable
fun CustomColorPickerDialog(
    initialColor: Long,
    onDismiss: () -> Unit,
    onColorSelected: (Long) -> Unit
) {
    val initCol = Color(initialColor)
    var red by remember { mutableFloatStateOf(initCol.red) }
    var green by remember { mutableFloatStateOf(initCol.green) }
    var blue by remember { mutableFloatStateOf(initCol.blue) }

    val currentColor = remember(red, green, blue) {
        Color(red, green, blue)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.color_picker_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(currentColor)
                            .border(3.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .testTag("custom_color_preview")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.color_picker_red, (red * 255).toInt()),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE53935)
                )
                Slider(
                    value = red,
                    onValueChange = { red = it },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFE53935),
                        activeTrackColor = Color(0xFFE53935)
                    ),
                    modifier = Modifier.testTag("slider_red")
                )

                Text(
                    text = stringResource(R.string.color_picker_green, (green * 255).toInt()),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF43A047)
                )
                Slider(
                    value = green,
                    onValueChange = { green = it },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF43A047),
                        activeTrackColor = Color(0xFF43A047)
                    ),
                    modifier = Modifier.testTag("slider_green")
                )

                Text(
                    text = stringResource(R.string.color_picker_blue, (blue * 255).toInt()),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E88E5)
                )
                Slider(
                    value = blue,
                    onValueChange = { blue = it },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF1E88E5),
                        activeTrackColor = Color(0xFF1E88E5)
                    ),
                    modifier = Modifier.testTag("slider_blue")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val argb = currentColor.toArgb().toLong() and 0xFFFFFFFFL
                    onColorSelected(argb)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PinkAccent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("custom_color_apply_btn")
            ) {
                Text(stringResource(R.string.color_picker_select), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("custom_color_cancel_btn")
            ) {
                Text(stringResource(R.string.dialog_cancel))
            }
        }
    )
}
