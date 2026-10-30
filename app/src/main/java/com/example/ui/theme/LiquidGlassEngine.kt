package com.example.ui.theme

import android.graphics.RenderEffect as AndroidRenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Spring-Physics animation specifications for iOS-quality fluid transitions
 */
object LiquidPhysics {
    val NavigationSpring = spring<Float>(
        dampingRatio = 0.68f, // Slight overshoot
        stiffness = Spring.StiffnessMediumLow
    )
    val PanelSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
    val TouchSpring = spring<Float>(
        dampingRatio = 0.75f,
        stiffness = 450f
    )
    val IndicatorSpring = spring<Float>(
        dampingRatio = 0.68f,
        stiffness = Spring.StiffnessMediumLow
    )
}

/**
 * Full-app Liquid Glass styling configuration matching exact web prototype specification:
 * - Transparency (0..100, default 65)
 * - Background Blur (0..30, default 8)
 * - Refraction (0..60, default 28)
 * - Lens Magnify (0..100, default 60)
 * - Edge Highlight & Bevel (0..100, default 60)
 * - Reflection (0..100, default 40)
 * - Tint Darkness (0..100, default 25)
 * - Corner Radius (10..32 dp, default 32)
 * - Animation Speed (20..200, default 100)
 * - Quality (Low / Medium / High)
 */
@Immutable
data class LiquidGlassStyleConfig(
    val isEnabled: Boolean = true,
    val quality: String = "High", // "Low", "Medium", "High"
    val transparency: Float = 65f, // 0..100 (Default 65)
    val blur: Float = 8f, // 0..30 (Default 8)
    val refraction: Float = 28f, // 0..60 (Default 28)
    val lensMagnify: Float = 60f, // 0..100 (Default 60)
    val edgeHighlight: Float = 60f, // 0..100 (Default 60)
    val reflection: Float = 40f, // 0..100 (Default 40)
    val tintDarkness: Float = 25f, // 0..100 (Default 25)
    val cornerRadiusDp: Float = 32f, // 10..32 (Default 32)
    val animSpeed: Float = 100f, // 20..200 (Default 100)
    val glassTintColorHex: Long = 0x35141928L,
    val isDarkGlass: Boolean = true,

    // Backward-compatibility properties with mathematical bridges:
    val blurRadius: Float = blur,
    val refractionAmount: Float = refraction / 60f,
    val edgeHighlights: Float = edgeHighlight / 100f,
    val reflectionIntensity: Float = reflection / 100f,
    val opticalTiltStrength: Float = refraction / 28f,
    val glassThickness: Float = 1.0f,
    val depthEnabled: Boolean = true,
    val vibrancy: Float = 0.85f,
    val shadowElevationDp: Float = 6f,
    val animationIntensity: Float = animSpeed / 100f,
    val performanceMode: String = quality,
    val glassIntensity: Float = (100f - transparency) / 100f,
    val refractionIntensity: Float = refraction / 60f,
    val specularHighlight: Boolean = edgeHighlight > 0f,
    val borderThicknessDp: Float = 1.0f,
    val isFrosted: Boolean = blur > 0f,
    val lightAngleDegrees: Float = 315f,
    val opticalShaderEnabled: Boolean = true,
    val springPhysicsEnabled: Boolean = true,
    val lensMagnification: Float = 1.0f + (lensMagnify / 35f),
    val animationSmoothnessMode: String = "Silky Smooth",

    // Neon Lights compatibility
    val neonEnabled: Boolean = false,
    val neonColorHex: Long = 0xFF00E5FFL,
    val neonIntensity: Float = 0.85f,
    val neonAnimated: Boolean = false,
    val neonAnimationSpeed: Float = 1.0f,

    // Drop reflection & extra compatibility
    val liquidDropReflectionEnabled: Boolean = false,
    val dropReflectionSize: Float = 0.55f,
    val dropReflectionIntensity: Float = 0.75f,
    val dropReflectionX: Float = 0.50f,
    val dropReflectionY: Float = 0.35f,
    val dropReflectionSoftness: Float = 0.65f,
    val dropSecondaryHighlight: Float = 0.45f,
    val convexCurvatureIntensity: Float = 0.70f,
    val bevelStrength: Float = 0.80f,
    val depthIntensity: Float = 0.75f,
    val glassSmoothness: Float = 0.85f,
    val edgeCurvaturePower: Float = 2.5f,
    val chromaticDispersion: Float = 0.45f,
    val layerRefraction: Boolean = true,
    val refractionHeight: Float = 0.65f,

    // Subscribe Button Customization
    val subscribeGlassEnabled: Boolean = true,
    val subscribeGlassTintHex: Long = 0xFFFFFFFFL,
    val subscribeTransparency: Float = 0.85f,
    val subscribeCornerRadius: Float = 18f,
    val subscribeDepth: Boolean = true,
    val subscribeRefraction: Float = 0.65f,
    val subscribeEdgeHighlight: Float = 0.75f
)

val LocalLiquidGlassConfig = staticCompositionLocalOf { LiquidGlassStyleConfig() }
val LocalFeedGraphicsLayer = staticCompositionLocalOf<androidx.compose.ui.graphics.layer.GraphicsLayer?> { null }

/**
 * Dimensional, physically-accurate iOS Liquid Glass Surface Modifier:
 * 
 * 1. Master Switch: Returns this when disabled (normal solid UI).
 * 2. Order of operations: Displace -> Blur -> Saturate (1.5) -> Tint Fill + Darkness -> Bevel / Sheen.
 * 3. Fill: tint color alpha = 0.5 * (1 - Transparency/100) + 0.03, black overlay alpha = Darkness/100 * 0.6.
 * 4. Edge and Bevel: 1dp border (Edge * 0.55), inner top highlight (Edge * 0.7), inner bottom highlight (Edge * 0.25), soft inner glow, soft drop shadow.
 * 5. Reflection: 115° diagonal soft white sheen band moving across surface, alpha = Reflection * 0.6, period = 7s / (animSpeed / 100).
 * 6. Quality Modes:
 *    - Low: Blur only (no refraction, no sheen animation)
 *    - Medium: Blur + refraction, static sheen
 *    - High: Everything animated in real time
 */
fun Modifier.liquidGlassSurface(
    shape: Shape,
    config: LiquidGlassStyleConfig,
    accentColor: Color = Color(0xFF38BDF8),
    elevation: Dp = 6.dp,
    interactiveGlow: Boolean = false
): Modifier {
    // 1. MASTER SWITCH: When disabled, return clean, standard UI without glass overhead
    if (!config.isEnabled && !config.neonEnabled) {
        return this
    }

    val isDark = config.isDarkGlass
    val isQualityLow = config.quality.equals("Low", ignoreCase = true)
    val isQualityMedium = config.quality.equals("Medium", ignoreCase = true)
    val isQualityHigh = !isQualityLow && !isQualityMedium

    // Quality integer: 0 = Low, 1 = Medium, 2 = High
    val qualityCode = when {
        isQualityLow -> 0f
        isQualityMedium -> 1f
        else -> 2f
    }

    // 2. FILL FORMULA:
    // Tint color alpha = 0.5 * (1 - Transparency/100) + 0.03
    val tintAlpha = (0.5f * (1f - config.transparency.coerceIn(0f, 100f) / 100f) + 0.03f).coerceIn(0.02f, 0.98f)
    // Black overlay alpha = (Darkness / 100) * 0.6
    val darkAlpha = ((config.tintDarkness.coerceIn(0f, 100f) / 100f) * 0.6f).coerceIn(0f, 0.60f)

    val customTintColor = Color(config.glassTintColorHex)
    val baseTintColor = if (customTintColor.alpha > 0.01f && config.glassTintColorHex != 0L) {
        customTintColor.copy(alpha = tintAlpha)
    } else if (isDark) {
        Color(0xFF141928).copy(alpha = tintAlpha)
    } else {
        Color(0xFFFFFFFF).copy(alpha = tintAlpha)
    }
    val darknessColor = Color.Black.copy(alpha = darkAlpha)

    // 3. EDGE HIGHLIGHT & BEVEL FORMULA:
    val edgeFactor = (config.edgeHighlight.coerceIn(0f, 100f) / 100f)
    val border1dpAlpha = (edgeFactor * 0.55f).coerceIn(0.05f, 0.85f)
    val innerTopAlpha = (edgeFactor * 0.70f).coerceIn(0f, 0.95f)
    val innerBottomAlpha = (edgeFactor * 0.25f).coerceIn(0f, 0.50f)
    val innerGlowAlpha = (edgeFactor * 0.15f).coerceIn(0f, 0.35f)

    // 4. REFLECTION FORMULA:
    val reflectionAlpha = ((config.reflection.coerceIn(0f, 100f) / 100f) * 0.6f).coerceIn(0f, 0.60f)

    // 5. SHADOW & DEPTH
    val shadowElevation = if (config.depthEnabled) {
        (config.shadowElevationDp.takeIf { it > 0f } ?: elevation.value).coerceIn(0f, 16f).dp
    } else 0.dp

    return this
        .then(
            if (shadowElevation > 0.dp) {
                Modifier.shadow(
                    elevation = shadowElevation,
                    shape = shape,
                    clip = false,
                    ambientColor = if (config.neonEnabled) Color(config.neonColorHex).copy(alpha = 0.35f) else Color(0x30000000),
                    spotColor = if (config.neonEnabled) Color(config.neonColorHex).copy(alpha = 0.45f) else Color(0x50000000)
                )
            } else Modifier
        )
        // 6. REAL-TIME GPU DISPLACEMENT & BLUR via AGSL (API 33+)
        .then(
            if (config.isEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Modifier.graphicsLayer {
                    if (size.width < 12f || size.height < 12f) {
                        renderEffect = null
                        return@graphicsLayer
                    }
                    try {
                        val shader = com.mytube.ui.glass.LiquidGlassShader.obtain()
                        shader.setFloatUniform("resolution", size.width, size.height)
                        shader.setFloatUniform("cornerRadius", config.cornerRadiusDp * density)
                        shader.setFloatUniform("refraction", if (isQualityLow) 0f else config.refraction)
                        shader.setFloatUniform("time", (System.currentTimeMillis() % 1000000L) / 1000f)
                        shader.setFloatUniform("animSpeed", config.animSpeed)
                        shader.setFloatUniform("edgeHighlight", config.edgeHighlight)
                        shader.setFloatUniform("reflection", config.reflection)
                        shader.setFloatUniform("transparency", config.transparency)
                        shader.setFloatUniform("tintDarkness", config.tintDarkness)
                        
                        val tint = customTintColor
                        shader.setFloatUniform("tintColor", tint.red, tint.green, tint.blue, 1.0f)
                        shader.setFloatUniform("qualityMode", qualityCode)
                        shader.setFloatUniform("density", density)

                        val shaderEffect = AndroidRenderEffect.createRuntimeShaderEffect(shader, "background")

                        // Chained hardware blur if blur > 0
                        val effectiveBlurPx = (config.blur * density * 0.45f).coerceIn(0f, 40f)
                        renderEffect = if (effectiveBlurPx > 0.5f && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val blurEffect = AndroidRenderEffect.createBlurEffect(
                                effectiveBlurPx,
                                effectiveBlurPx,
                                Shader.TileMode.CLAMP
                            )
                            AndroidRenderEffect.createChainEffect(blurEffect, shaderEffect).asComposeRenderEffect()
                        } else {
                            shaderEffect.asComposeRenderEffect()
                        }
                    } catch (_: Exception) {
                        renderEffect = null
                    }
                }
            } else Modifier
        )
        .clip(shape)
        .drawBehind {
            // A. Base translucent liquid glass tint fill
            drawRect(color = baseTintColor)

            // B. Darkness overlay
            if (darkAlpha > 0.01f) {
                drawRect(color = darknessColor)
            }

            // C. Layered Blur diffusion simulation for pre-API 33 or fallback
            if (config.blur > 0.5f) {
                val blurFactor = (config.blur / 30f).coerceIn(0.05f, 1f)
                val blurBrush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.07f * blurFactor),
                        Color(0xFF38BDF8).copy(alpha = 0.03f * blurFactor),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.5f),
                    radius = (size.width.coerceAtLeast(size.height)) * 0.85f
                )
                drawRect(brush = blurBrush)
            }

            // D. Inner Top Highlight (alpha = Edge * 0.70)
            if (innerTopAlpha > 0.02f) {
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = innerTopAlpha),
                            Color.Transparent
                        )
                    ),
                    start = Offset(size.width * 0.08f, 1f),
                    end = Offset(size.width * 0.92f, 1f),
                    strokeWidth = 1.5.dp.toPx()
                )
            }

            // E. Inner Bottom Highlight (alpha = Edge * 0.25)
            if (innerBottomAlpha > 0.02f) {
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = innerBottomAlpha),
                            Color.Transparent
                        )
                    ),
                    start = Offset(size.width * 0.12f, size.height - 1f),
                    end = Offset(size.width * 0.88f, size.height - 1f),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // F. Soft Inner Glow
            if (innerGlowAlpha > 0.02f) {
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = innerGlowAlpha),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.5f),
                        radius = (size.width.coerceAtLeast(size.height)) * 0.55f
                    ),
                    topLeft = Offset(2f, 2f),
                    size = Size(size.width - 4f, size.height - 4f),
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // G. Diagonal (115°) Soft Sheen Band across surface (Fallback / Medium / Layer)
            if (reflectionAlpha > 0.02f && !isQualityLow) {
                val sheenBrush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = reflectionAlpha * 0.85f),
                        Color.White.copy(alpha = reflectionAlpha * 0.35f),
                        Color.Transparent
                    ),
                    start = Offset(size.width * 0.15f, 0f),
                    end = Offset(size.width * 0.85f, size.height)
                )
                drawRect(brush = sheenBrush)
            }

            // H. Neon Glow (if enabled)
            if (config.neonEnabled) {
                val neonGlowColor = Color(config.neonColorHex).copy(alpha = 0.22f * config.neonIntensity)
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(neonGlowColor, Color.Transparent),
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = (size.width.coerceAtLeast(size.height)) * 0.90f
                    )
                )
            }
        }
        // 7. 1DP BORDER WITH ALPHA = EDGE * 0.55
        .border(
            width = 1.dp,
            brush = if (config.neonEnabled) {
                Brush.sweepGradient(
                    listOf(
                        Color(config.neonColorHex),
                        Color.White,
                        Color(config.neonColorHex)
                    )
                )
            } else {
                SolidColor(Color.White.copy(alpha = border1dpAlpha))
            },
            shape = shape
        )
}

/**
 * Modifier for interactive buttons, chips, and small controls with animated specular sheen
 */
@Composable
fun Modifier.liquidGlassInteractive(
    shape: Shape,
    config: LiquidGlassStyleConfig,
    accentColor: Color = LocalAppAccentColor.current,
    elevation: Dp = 4.dp
): Modifier {
    val animFactor = (config.animSpeed / 100f).coerceIn(0.2f, 3.0f)
    val infiniteTransition = rememberInfiniteTransition(label = "interactive_glass_sheen")
    val sweepOffset by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (7000 / animFactor).toInt().coerceAtLeast(1000),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "sheen_anim"
    )

    return this
        .liquidGlassSurface(shape, config, accentColor, elevation)
        .then(
            if (config.neonEnabled && config.neonAnimated) {
                Modifier.drawWithContent {
                    drawContent()
                    val pulseAlpha = (sweepOffset % 300f) / 300f
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(config.neonColorHex).copy(alpha = 0.25f * pulseAlpha * config.animationIntensity),
                                Color.Transparent
                            ),
                            startX = sweepOffset - 120f,
                            endX = sweepOffset + 120f
                        )
                    )
                }
            } else Modifier
        )
}
