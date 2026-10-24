package com.example.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.YtBorder
import com.example.ui.theme.YtSurfaceDark
import com.example.ui.theme.YtSurfaceVariant
import com.example.ui.theme.YtTextPrimary
import com.example.ui.theme.YtTextSecondary

/**
 * Full-Spectrum Color Picker with:
 * - 2D Saturation / Value (Brightness) interactive gradient canvas with draggable cursor
 * - 1D Hue rainbow spectrum slider (0°..360°)
 * - Live Color Preview swatch
 * - Real-time formatted HEX and RGB readouts
 * - Manual HEX input field with instant validation and apply
 * - Preset colors as optional shortcuts
 */
@Composable
fun FullSpectrumColorPicker(
    initialColorHex: Long,
    onColorChanged: (Long) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Color Picker",
    showPresets: Boolean = true
) {
    // Convert initial color to HSV
    val initialInt = initialColorHex.toInt()
    val initialHsv = remember(initialColorHex) {
        val hsv = FloatArray(3)
        AndroidColor.colorToHSV(initialInt, hsv)
        hsv
    }

    var hue by remember(initialColorHex) { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember(initialColorHex) { mutableFloatStateOf(initialHsv[1].coerceIn(0f, 1f)) }
    var value by remember(initialColorHex) { mutableFloatStateOf(initialHsv[2].coerceIn(0f, 1f)) }

    // Computed current color
    val currentColorInt = remember(hue, saturation, value) {
        AndroidColor.HSVToColor(floatArrayOf(hue, saturation, value))
    }
    val currentColorLong = remember(currentColorInt) {
        currentColorInt.toLong() and 0xFFFFFFFFL
    }
    val currentColor = Color(currentColorLong)

    // Formatted strings
    val hexString = remember(currentColorInt) {
        String.format("#%06X", 0xFFFFFF and currentColorInt)
    }
    val rgbString = remember(currentColorInt) {
        val r = AndroidColor.red(currentColorInt)
        val g = AndroidColor.green(currentColorInt)
        val b = AndroidColor.blue(currentColorInt)
        "R: $r  G: $g  B: $b"
    }

    var manualHexInput by remember(hexString) { mutableStateOf(hexString) }
    val focusManager = LocalFocusManager.current

    val presetColors = listOf(
        "Cyan" to 0xFF00E5FFL,
        "Pink" to 0xFFFF4081L,
        "Red" to 0xFFFF1744L,
        "Orange" to 0xFFFF9100L,
        "Yellow" to 0xFFFFEA00L,
        "Green" to 0xFF00E676L,
        "Blue" to 0xFF2979FFL,
        "Purple" to 0xFFD500F9L,
        "White" to 0xFFFFFFFFL,
        "Slate" to 0xFF90A4AEL
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(YtSurfaceDark)
            .border(1.dp, YtBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
            .testTag("full_spectrum_color_picker")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = currentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = YtTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Live Swatch + Hex badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp, 22.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(currentColor)
                        .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = hexString,
                    color = YtTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 1. 2D SATURATION & VALUE (BRIGHTNESS) CANVAS
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
        ) {
            // Pure hue base color
            val pureHueColor = Color(AndroidColor.HSVToColor(floatArrayOf(hue, 1f, 1f)))

            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(hue) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                saturation = (offset.x / size.width).coerceIn(0f, 1f)
                                value = (1f - (offset.y / size.height)).coerceIn(0f, 1f)
                                onColorChanged(currentColorLong)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                saturation = (change.position.x / size.width).coerceIn(0f, 1f)
                                value = (1f - (change.position.y / size.height)).coerceIn(0f, 1f)
                                onColorChanged(currentColorLong)
                            }
                        )
                    }
            ) {
                // Horizontal white to pure-hue gradient
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.White, pureHueColor)
                    )
                )
                // Vertical transparent to black gradient
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black)
                    )
                )

                // Draggable indicator cursor
                val cursorX = saturation * size.width
                val cursorY = (1f - value) * size.height

                drawCircle(
                    color = Color.Black,
                    radius = 9.dp.toPx(),
                    center = Offset(cursorX, cursorY),
                    style = Stroke(width = 2.5.dp.toPx())
                )
                drawCircle(
                    color = Color.White,
                    radius = 7.dp.toPx(),
                    center = Offset(cursorX, cursorY),
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    color = currentColor,
                    radius = 5.dp.toPx(),
                    center = Offset(cursorX, cursorY)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. 1D HUE SPECTRUM RAINBOW BAR
        Text(
            text = "Hue Spectrum (${hue.toInt()}°)",
            color = YtTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
        ) {
            val rainbowColors = listOf(
                Color.Red,
                Color.Yellow,
                Color.Green,
                Color.Cyan,
                Color.Blue,
                Color.Magenta,
                Color.Red
            )

            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                hue = (offset.x / size.width * 360f).coerceIn(0f, 360f)
                                onColorChanged(currentColorLong)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                hue = (change.position.x / size.width * 360f).coerceIn(0f, 360f)
                                onColorChanged(currentColorLong)
                            }
                        )
                    }
            ) {
                drawRect(brush = Brush.horizontalGradient(rainbowColors))

                val cursorX = (hue / 360f) * size.width
                drawLine(
                    color = Color.Black,
                    start = Offset(cursorX, 0f),
                    end = Offset(cursorX, size.height),
                    strokeWidth = 4.dp.toPx()
                )
                drawLine(
                    color = Color.White,
                    start = Offset(cursorX, 0f),
                    end = Offset(cursorX, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. RGB READOUT & MANUAL HEX INPUT
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = rgbString,
                color = YtTextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(96.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(YtSurfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = manualHexInput,
                        onValueChange = { manualHexInput = it.uppercase() },
                        textStyle = TextStyle(
                            color = YtTextPrimary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(currentColor),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                try {
                                    val parsed = parseHexColor(manualHexInput)
                                    if (parsed != null) {
                                        val hsv = FloatArray(3)
                                        AndroidColor.colorToHSV(parsed.toInt(), hsv)
                                        hue = hsv[0]
                                        saturation = hsv[1]
                                        value = hsv[2]
                                        onColorChanged(parsed)
                                    }
                                } catch (_: Exception) {}
                            }
                        )
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                OutlinedButton(
                    onClick = {
                        focusManager.clearFocus()
                        try {
                            val parsed = parseHexColor(manualHexInput)
                            if (parsed != null) {
                                val hsv = FloatArray(3)
                                AndroidColor.colorToHSV(parsed.toInt(), hsv)
                                hue = hsv[0]
                                saturation = hsv[1]
                                value = hsv[2]
                                onColorChanged(parsed)
                            }
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier.height(32.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Apply", fontSize = 11.sp)
                }
            }
        }

        // 4. PRESET COLOR SHORTCUTS
        if (showPresets) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Preset Shortcuts",
                color = YtTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presetColors.forEach { (name, hex) ->
                    val isSelected = currentColorLong == hex
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(hex))
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                            .clickable {
                                val hsv = FloatArray(3)
                                AndroidColor.colorToHSV(hex.toInt(), hsv)
                                hue = hsv[0]
                                saturation = hsv[1]
                                value = hsv[2]
                                onColorChanged(hex)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = name,
                                tint = if (hex == 0xFFFFFFFFL) Color.Black else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog wrapper for the full-spectrum color picker
 */
@Composable
fun FullSpectrumColorPickerDialog(
    title: String,
    initialColorHex: Long,
    onDismiss: () -> Unit,
    onColorSelected: (Long) -> Unit
) {
    var selectedColorHex by remember(initialColorHex) { mutableStateOf(initialColorHex) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            FullSpectrumColorPicker(
                initialColorHex = initialColorHex,
                onColorChanged = { selectedColorHex = it },
                title = title
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onColorSelected(selectedColorHex)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(selectedColorHex))
            ) {
                Text(
                    text = "Select Color",
                    color = if (Color(selectedColorHex).red > 0.8f && Color(selectedColorHex).green > 0.8f && Color(selectedColorHex).blue > 0.8f) Color.Black else Color.White
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = YtTextSecondary)
            }
        },
        containerColor = YtSurfaceDark
    )
}

private fun parseHexColor(hex: String): Long? {
    val clean = hex.trim().removePrefix("#")
    return try {
        when (clean.length) {
            6 -> 0xFF000000L or clean.toLong(16)
            8 -> clean.toLong(16)
            3 -> {
                val r = clean[0]
                val g = clean[1]
                val b = clean[2]
                0xFF000000L or "$r$r$g$g$b$b".toLong(16)
            }
            else -> null
        }
    } catch (_: Exception) {
        null
    }
}
