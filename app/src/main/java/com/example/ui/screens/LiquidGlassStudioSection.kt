package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SettingsManager
import com.example.ui.components.FullSpectrumColorPicker
import com.example.ui.theme.LiquidGlassStyleConfig
import com.example.ui.theme.YtBorder
import com.example.ui.theme.YtSurfaceDark
import com.example.ui.theme.YtSurfaceVariant
import com.example.ui.theme.YtTextPrimary
import com.example.ui.theme.YtTextSecondary
import com.example.ui.theme.liquidGlassSurface

/**
 * iOS 27 Liquid Glass Studio Settings:
 * 1. Pinned Live Preview Card at top (vibrant backdrop + floating glass pill + mini lens).
 * 2. Grouped Glass Cards:
 *    - Basics: Master switch, Presets (Clear, Frosted, Crystal, Dark), Quality (Low, Medium, High).
 *    - Optics: Transparency (0-100), Blur (0-30), Refraction (0-60), Lens Magnify (0-100).
 *    - Light: Edge Highlight & Bevel (0-100), Reflection (0-100), Tint Color, Tint Darkness (0-100).
 *    - Shape: Corner Radius (10-32).
 *    - Performance: Animation Speed (20-200), Reset All Settings.
 * 3. Section Reset buttons + Reset All.
 * 4. Master switch turns off glass cleanly (disabled UI feedback).
 */
fun LazyListScope.liquidGlassStudioItems(
    settingsManager: SettingsManager,
    interfaceBuilderManager: Any? = null,
    glassConfig: LiquidGlassStyleConfig,
    accentColor: Color,
    context: Context,
    lgMaster: Boolean,
    lgTransparency: Float = 0.65f,
    lgBlur: Float = 8f,
    lgCornerRadius: Float = 32f,
    lgRefractionHeight: Float = 0.65f,
    lgRefraction: Float = 28f,
    lgLayerRefraction: Boolean = true,
    lgDepthEnabled: Boolean = true,
    lgVibrancy: Float = 0.85f,
    lgEdgeHighlights: Float = 0.60f,
    lgBorderThickness: Float = 1.0f,
    lgReflection: Float = 0.40f,
    lgTintColorHex: Long = 0x35141928L,
    lgShadowElevation: Float = 6f,
    lgAnimIntensity: Float = 1.0f,
    lgPerfMode: String = "High",
    lgSpecular: Boolean = true,
    opticalChromaticDispersion: Float = 0.45f,
    neonMaster: Boolean = false,
    neonColorHex: Long = 0xFF00E5FFL,
    neonIntensity: Float = 0.85f,
    neonAnimated: Boolean = false,
    neonSpeed: Float = 1.0f,
    subGlassEnabled: Boolean = true,
    subGlassTintHex: Long = 0xFFFFFFFFL,
    subTransparency: Float = 0.85f,
    subCornerRadius: Float = 18f,
    subDepth: Boolean = true,
    subRefraction: Float = 0.65f,
    subEdgeHighlight: Float = 0.75f,
    currentTheme: String = "Dark Mode",
    isAdvancedMode: Boolean = false,
    onToggleAdvancedMode: ((Boolean) -> Unit)? = null
) {
    liquidGlassStudioItems(
        settingsManager = settingsManager,
        glassConfig = glassConfig,
        accentColor = accentColor,
        context = context,
        lgMaster = lgMaster,
        lgQuality = if (lgPerfMode.equals("Low", ignoreCase = true)) "Low" else if (lgPerfMode.equals("Medium", ignoreCase = true)) "Medium" else "High",
        lgTransparency = if (lgTransparency <= 1.0f) (lgTransparency * 100).toInt() else lgTransparency.toInt(),
        lgBlur = lgBlur.toInt(),
        lgRefraction = if (lgRefraction <= 1.0f) (lgRefraction * 60).toInt() else lgRefraction.toInt(),
        lgLensMagnify = 60,
        lgEdgeHighlight = if (lgEdgeHighlights <= 1.0f) (lgEdgeHighlights * 100).toInt() else lgEdgeHighlights.toInt(),
        lgReflection = if (lgReflection <= 1.0f) (lgReflection * 100).toInt() else lgReflection.toInt(),
        lgDarkness = 25,
        lgCornerRadius = lgCornerRadius.toInt(),
        lgAnimSpeed = if (lgAnimIntensity <= 2.0f) (lgAnimIntensity * 100).toInt() else lgAnimIntensity.toInt(),
        lgTintColorHex = lgTintColorHex
    )
}

fun LazyListScope.liquidGlassStudioItems(
    settingsManager: SettingsManager,
    glassConfig: LiquidGlassStyleConfig,
    accentColor: Color,
    context: Context,
    lgMaster: Boolean,
    lgQuality: String,
    lgTransparency: Int,
    lgBlur: Int,
    lgRefraction: Int,
    lgLensMagnify: Int,
    lgEdgeHighlight: Int,
    lgReflection: Int,
    lgDarkness: Int,
    lgCornerRadius: Int,
    lgAnimSpeed: Int,
    lgTintColorHex: Long
) {
    // 1. PINNED LIVE PREVIEW CARD AT TOP
    item {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(20.dp))
                .height(180.dp)
        ) {
            // Vivid Scenic / Abstract Test Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Deep gradient backdrop
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0F172A),
                            Color(0xFF1E1B4B),
                            Color(0xFF0284C7),
                            Color(0xFFE11D48)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(w, h)
                    )
                )

                // High contrast optical stripes and target discs (displaced through refraction)
                for (i in 0..6) {
                    val y = h * (0.15f + i * 0.12f)
                    drawLine(
                        color = if (i % 2 == 0) Color(0xFF38BDF8).copy(alpha = 0.6f) else Color(0xFFF43F5E).copy(alpha = 0.6f),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 3.dp.toPx()
                    )
                }

                // Vibrant circles in backdrop to clearly show lens magnification & edge bending
                drawCircle(
                    color = Color(0xFFFACC15).copy(alpha = 0.8f),
                    radius = 28.dp.toPx(),
                    center = Offset(w * 0.28f, h * 0.45f)
                )
                drawCircle(
                    color = Color(0xFFA855F7).copy(alpha = 0.8f),
                    radius = 36.dp.toPx(),
                    center = Offset(w * 0.72f, h * 0.55f)
                )
            }

            // Preview Info Banner & Floating Glass Pill
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (lgMaster) Color(0xFF4ADE80) else Color(0xFFF87171))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (lgMaster) "LIVE GLASS PREVIEW" else "GLASS OFF (SOLID PREVIEW)",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Text(
                        text = "Quality: $lgQuality",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Floating Liquid Glass Navigation Pill with Mini Lens
                val pillCornerRadius = lgCornerRadius.dp
                val pillShape = RoundedCornerShape(pillCornerRadius)

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .height(58.dp)
                        .then(
                            if (lgMaster) {
                                Modifier.liquidGlassSurface(
                                    shape = pillShape,
                                    config = glassConfig,
                                    accentColor = accentColor,
                                    elevation = 8.dp
                                )
                            } else {
                                Modifier
                                    .clip(pillShape)
                                    .background(Color(0xFF1E1E1E).copy(alpha = 0.95f))
                                    .border(0.8.dp, Color.White.copy(alpha = 0.2f), pillShape)
                            }
                        )
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Lens capsule over 2nd item
                        Box(
                            modifier = Modifier
                                .size(width = 54.dp, height = 44.dp)
                                .then(
                                    if (lgMaster) {
                                        val miniLensConfig = glassConfig.copy(
                                            refraction = (glassConfig.refraction * (1f + glassConfig.lensMagnify / 35f)).coerceIn(0f, 180f),
                                            cornerRadiusDp = (lgCornerRadius - 4).coerceAtLeast(10).toFloat()
                                        )
                                        Modifier.liquidGlassSurface(
                                            shape = RoundedCornerShape((lgCornerRadius - 4).coerceAtLeast(10).dp),
                                            config = miniLensConfig,
                                            accentColor = accentColor,
                                            elevation = 3.dp
                                        )
                                    } else {
                                        Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White.copy(alpha = 0.15f))
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Diamond,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(20.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(20.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = if (lgMaster) "Real-time background optical refraction active" else "Glass disabled — standard flat UI",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            }
        }
    }

    // 2. BASICS CARD
    item {
        StudioSectionCard(
            title = "Basics",
            icon = Icons.Default.Diamond,
            accentColor = accentColor,
            onReset = { settingsManager.resetBasics() }
        ) {
            // Master Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Enable Liquid Glass",
                        color = YtTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (lgMaster) "Translucent optical glass enabled" else "Off — normal solid flat UI",
                        color = if (lgMaster) accentColor else YtTextSecondary,
                        fontSize = 12.sp
                    )
                }
                Switch(
                    checked = lgMaster,
                    onCheckedChange = { settingsManager.setLiquidGlassMaster(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = accentColor
                    ),
                    modifier = Modifier.testTag("switch_liquid_glass_master")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Presets: Clear, Frosted, Crystal, Dark
            Text(
                text = "Glass Presets",
                color = YtTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val presets = listOf("Crystal", "Clear", "Frosted", "Dark")
                presets.forEach { preset ->
                    val isSelected = when (preset) {
                        "Crystal" -> lgTransparency == 65 && lgBlur == 8 && lgRefraction == 28
                        "Clear" -> lgTransparency == 90 && lgBlur == 0
                        "Frosted" -> lgTransparency == 45 && lgBlur == 22
                        "Dark" -> lgTransparency == 50 && lgDarkness == 65
                        else -> false
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected && lgMaster) accentColor else YtSurfaceVariant)
                            .clickable(enabled = lgMaster) {
                                when (preset) {
                                    "Crystal" -> settingsManager.applyPresetCrystal()
                                    "Clear" -> settingsManager.applyPresetClear()
                                    "Frosted" -> settingsManager.applyPresetFrosted()
                                    "Dark" -> settingsManager.applyPresetDark()
                                }
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = preset,
                            color = if (isSelected && lgMaster) Color.White else YtTextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quality Mode: Low / Medium / High
            Text(
                text = "Rendering Quality",
                color = YtTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Low" to "Blur Only",
                    "Medium" to "Static Sheen",
                    "High" to "Full 60fps"
                ).forEach { (mode, subtitle) ->
                    val isSelected = lgQuality.equals(mode, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected && lgMaster) accentColor else YtSurfaceVariant)
                            .clickable(enabled = lgMaster) { settingsManager.setLiquidGlassQuality(mode) }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = mode,
                                color = if (isSelected && lgMaster) Color.White else YtTextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                            Text(
                                text = subtitle,
                                color = if (isSelected && lgMaster) Color.White.copy(alpha = 0.8f) else YtTextSecondary,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // 3. OPTICS CARD
    item {
        StudioSectionCard(
            title = "Optics & Refraction",
            icon = Icons.Default.Tune,
            accentColor = accentColor,
            enabled = lgMaster,
            onReset = { settingsManager.resetOptics() }
        ) {
            // Transparency: 0..100 (Default 65)
            StudioSlider(
                label = "Transparency",
                value = lgTransparency.toFloat(),
                valueRange = 0f..100f,
                displayValue = "$lgTransparency%",
                enabled = lgMaster,
                accentColor = accentColor,
                onValueChange = { settingsManager.setLiquidGlassTransparency(it.toInt()) }
            )

            // Background Blur: 0..30 (Default 8)
            StudioSlider(
                label = "Background Blur",
                value = lgBlur.toFloat(),
                valueRange = 0f..30f,
                displayValue = "$lgBlur px",
                enabled = lgMaster,
                accentColor = accentColor,
                onValueChange = { settingsManager.setLiquidGlassBlur(it.toInt()) }
            )

            // Refraction: 0..60 (Default 28)
            StudioSlider(
                label = "Refraction / Tilt (Edge Inward Normal)",
                value = lgRefraction.toFloat(),
                valueRange = 0f..60f,
                displayValue = "$lgRefraction",
                enabled = lgMaster,
                accentColor = accentColor,
                onValueChange = { settingsManager.setLiquidGlassRefraction(it.toInt()) }
            )

            // Lens Magnification: 0..100 (Default 60)
            StudioSlider(
                label = "Lens Capsule Magnification",
                value = lgLensMagnify.toFloat(),
                valueRange = 0f..100f,
                displayValue = "$lgLensMagnify%",
                enabled = lgMaster,
                accentColor = accentColor,
                onValueChange = { settingsManager.setLiquidGlassLensMagnify(it.toInt()) }
            )
        }
    }

    // 4. LIGHT & HIGHLIGHTS CARD
    item {
        StudioSectionCard(
            title = "Light & Highlights",
            icon = Icons.Default.Lightbulb,
            accentColor = accentColor,
            enabled = lgMaster,
            onReset = { settingsManager.resetLight() }
        ) {
            // Edge Highlight & Bevel: 0..100 (Default 60)
            StudioSlider(
                label = "Edge Highlight & Bevel (Top/Bottom)",
                value = lgEdgeHighlight.toFloat(),
                valueRange = 0f..100f,
                displayValue = "$lgEdgeHighlight%",
                enabled = lgMaster,
                accentColor = accentColor,
                onValueChange = { settingsManager.setLiquidGlassEdgeHighlight(it.toInt()) }
            )

            // Reflection: 0..100 (Default 40)
            StudioSlider(
                label = "Reflection Sheen Intensity (115° Band)",
                value = lgReflection.toFloat(),
                valueRange = 0f..100f,
                displayValue = "$lgReflection%",
                enabled = lgMaster,
                accentColor = accentColor,
                onValueChange = { settingsManager.setLiquidGlassReflection(it.toInt()) }
            )

            // Tint Darkness: 0..100 (Default 25)
            StudioSlider(
                label = "Tint Darkness Overlay",
                value = lgDarkness.toFloat(),
                valueRange = 0f..100f,
                displayValue = "$lgDarkness%",
                enabled = lgMaster,
                accentColor = accentColor,
                onValueChange = { settingsManager.setLiquidGlassDarkness(it.toInt()) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Glass Tint Color Picker
            Text(
                text = "Glass Tint Color",
                color = YtTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            FullSpectrumColorPicker(
                title = "Glass Tint Palette",
                initialColorHex = lgTintColorHex,
                onColorChanged = { settingsManager.setLiquidGlassTintColorHex(it) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // 5. SHAPE CARD
    item {
        StudioSectionCard(
            title = "Shape & Geometry",
            icon = Icons.Default.Layers,
            accentColor = accentColor,
            enabled = lgMaster,
            onReset = { settingsManager.resetShape() }
        ) {
            // Corner Radius: 10..32 dp (Default 32)
            StudioSlider(
                label = "Pill Corner Radius",
                value = lgCornerRadius.toFloat(),
                valueRange = 10f..32f,
                displayValue = "$lgCornerRadius dp",
                enabled = lgMaster,
                accentColor = accentColor,
                onValueChange = { settingsManager.setLiquidGlassCornerRadius(it.toInt()) }
            )
        }
    }

    // 6. PERFORMANCE & MASTER RESET CARD
    item {
        StudioSectionCard(
            title = "Performance & Timing",
            icon = Icons.Default.Speed,
            accentColor = accentColor,
            enabled = lgMaster,
            onReset = { settingsManager.resetPerformance() }
        ) {
            // Animation Speed: 20..200 (Default 100)
            StudioSlider(
                label = "Animation Speed (Sheen & Transitions)",
                value = lgAnimSpeed.toFloat(),
                valueRange = 20f..200f,
                displayValue = "$lgAnimSpeed%",
                enabled = lgMaster,
                accentColor = accentColor,
                onValueChange = { settingsManager.setLiquidGlassAnimSpeed(it.toInt()) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Reset All Settings Button
            OutlinedButton(
                onClick = {
                    settingsManager.resetAllLiquidGlass()
                    Toast.makeText(context, "All Liquid Glass settings restored to Crystal defaults", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_reset_all_liquid_glass"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF87171)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF87171).copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset All to Crystal Defaults", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StudioSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    enabled: Boolean = true,
    onReset: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(YtSurfaceDark)
            .border(1.dp, YtBorder, RoundedCornerShape(16.dp))
            .alpha(if (enabled) 1f else 0.45f)
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (enabled) accentColor else YtTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        color = YtTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (onReset != null && enabled) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onReset() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset section",
                            tint = YtTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Reset",
                            color = YtTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun StudioSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    displayValue: String,
    enabled: Boolean,
    accentColor: Color,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = YtTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = displayValue,
                color = if (enabled) accentColor else YtTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = if (enabled) accentColor else Color.Gray,
                activeTrackColor = if (enabled) accentColor else Color.Gray.copy(alpha = 0.5f)
            )
        )
    }
}
