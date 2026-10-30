package com.example.launcher.glass

import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.launcher.model.GlassSettings

val LocalLauncherGlassSettings = compositionLocalOf { GlassSettings() }

/**
 * Reusable authentic Liquid Glass surface.
 * 
 * Strict 3-layer architecture:
 * 1. Sibling glass background (translucency, specular rim highlight, inner bevel shine, diagonal sheen)
 * 2. Foreground content drawn strictly AFTER and ABOVE glass (never blurred, never distorted)
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    cornerRadius: Dp? = null,
    glassSettings: GlassSettings = LocalLauncherGlassSettings.current,
    tintColor: Color = Color.White,
    content: @Composable () -> Unit
) {
    val radius = cornerRadius ?: glassSettings.cornerRadiusDp.dp
    val resolvedShape = shape ?: RoundedCornerShape(radius)

    // Global ON / OFF switch: when OFF, return clean solid non-glass iOS surface
    if (!glassSettings.isEnabled) {
        Box(
            modifier = modifier
                .clip(resolvedShape)
                .background(Color(0xFF202026).copy(alpha = 0.92f))
                .border(
                    BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    resolvedShape
                )
        ) {
            content()
        }
        return
    }

    // Sheen reflection animation phase (High performance mode only)
    val isHighPerf = glassSettings.performanceMode == "HIGH"
    val infiniteTransition = rememberInfiniteTransition(label = "glass_sheen")
    val sheenProgress by if (isHighPerf && glassSettings.reflectionSheen > 5f) {
        val durationMs = (7000 / (glassSettings.animSpeed / 100f).coerceIn(0.2f, 3.0f)).toInt()
        infiniteTransition.animateFloat(
            initialValue = -0.4f,
            targetValue = 1.4f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMs, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "sheen_phase"
        )
    } else {
        remember { androidx.compose.runtime.mutableFloatStateOf(0.35f) }
    }

    val alphaBase = (1f - glassSettings.transparency).coerceIn(0.08f, 0.95f)
    val darkOverlay = (glassSettings.tintDarkness / 100f * 0.45f).coerceIn(0f, 0.6f)
    val rimAlpha = (glassSettings.edgeHighlightStrength / 100f * 0.70f).coerceIn(0.1f, 0.9f)
    val reflectionAlpha = (glassSettings.reflectionSheen / 100f * 0.35f).coerceIn(0f, 0.5f)

    Box(
        modifier = modifier
            .clip(resolvedShape)
            .drawBehind {
                val w = size.width
                val h = size.height

                // 1. Frosted translucent base fill
                drawRect(
                    color = tintColor.copy(alpha = alphaBase * 0.22f)
                )
                // Darkness scrim for readability against bright wallpapers
                if (darkOverlay > 0f) {
                    drawRect(
                        color = Color.Black.copy(alpha = darkOverlay)
                    )
                }

                // 2. Inner bevel gradient (top-left light source simulation)
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = rimAlpha * 0.28f),
                            Color.White.copy(alpha = rimAlpha * 0.05f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.18f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(w, h)
                    )
                )

                // 3. Diagonal 115° moving reflection sheen band
                if (reflectionAlpha > 0.02f) {
                    val sheenCenter = Offset(w * sheenProgress, h * sheenProgress)
                    val bandRadius = (w.coerceAtLeast(h) * 0.45f)
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = reflectionAlpha),
                                Color.Transparent
                            ),
                            start = Offset(sheenCenter.x - bandRadius, sheenCenter.y - bandRadius),
                            end = Offset(sheenCenter.x + bandRadius, sheenCenter.y + bandRadius)
                        )
                    )
                }
            }
            .border(
                BorderStroke(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = rimAlpha),
                            Color.White.copy(alpha = rimAlpha * 0.4f),
                            Color.White.copy(alpha = 0.08f),
                            Color.White.copy(alpha = rimAlpha * 0.25f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                ),
                resolvedShape
            )
    ) {
        // Foreground content stays completely sharp, crisp, unblurred and fully interactive!
        content()
    }
}
