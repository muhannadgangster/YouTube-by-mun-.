package com.example.ui.components

import android.graphics.RenderEffect as AndroidRenderEffect
import android.graphics.Shader
import android.os.Build
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.LocalAppAccentColor
import com.example.ui.theme.LocalFeedGraphicsLayer
import com.example.ui.theme.LocalLiquidGlassConfig
import com.example.ui.theme.LocalUiCustomization
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/**
 * YouTube Navigation Bar with exact iOS Liquid Glass 3-Layer Sibling Architecture:
 *
 * ROOT CAUSE FIX:
 * Glass is NEVER a parent of content! No Modifier.blur(), RenderEffect or alpha on
 * containers that hold icons, text, thumbnails or buttons.
 *
 * SIBLING LAYER 1: BASE LIQUID GLASS DOCK SURFACE (no children)
 * - Captures real feed/Shorts content passing underneath via LocalFeedGraphicsLayer
 * - Samples and displaces the background content using AGSL SDF displacement shader
 * - Saturates (1.5), applies tint fill, darkness overlay, 1dp gradient border, sheen reflection
 *
 * SIBLING LAYER 2: LENS CAPSULE (no children)
 * - Positioned behind the active tab
 * - Stronger refraction = refraction * (1 + lensMagnify / 35)
 * - Press-and-hold interaction: scale grows to 1.15, higher transparency
 * - Horizontal drag gesture across tabs following finger smoothly; snaps on release
 *   with spring animation (stiffness = 300, damping = 0.68f) and slight overshoot
 *
 * SIBLING LAYER 3: FOREGROUND ITEMS (Home, Shorts, Add +, Subscriptions, You)
 * - Drawn AFTER and ABOVE the glass layer
 * - ZERO shader, ZERO blur, ZERO RenderEffect, ZERO tilt
 * - 100% crisp, flat, sharp, and clickable!
 */
@Composable
fun YouTubeBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onAddClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val accentColor = LocalAppAccentColor.current
    val glassConfig = LocalLiquidGlassConfig.current
    val customization = LocalUiCustomization.current
    val feedGraphicsLayer = LocalFeedGraphicsLayer.current
    val isGlass = glassConfig.isEnabled && customization.navBarGlassEnabled
    val activeColor = if (accentColor != Color.Unspecified && accentColor != Color.Transparent) accentColor else Color.White
    val inactiveColor = Color.White.copy(alpha = 0.72f)
    val showLabels = customization.navBarShowLabels
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    // Corner radius from user settings (10..32 dp)
    val cornerRadius = glassConfig.cornerRadiusDp.dp.coerceIn(10.dp, 32.dp)
    val dockShape = RoundedCornerShape(cornerRadius)

    val isQualityLow = glassConfig.quality.equals("Low", ignoreCase = true)
    val isQualityMedium = glassConfig.quality.equals("Medium", ignoreCase = true)
    val qualityCode = when {
        isQualityLow -> 0f
        isQualityMedium -> 1f
        else -> 2f
    }

    // Colors and alphas matching exact physical liquid glass spec
    val tintAlpha = (0.5f * (1f - glassConfig.transparency.coerceIn(0f, 100f) / 100f) + 0.03f).coerceIn(0.02f, 0.98f)
    val darkAlpha = ((glassConfig.tintDarkness.coerceIn(0f, 100f) / 100f) * 0.6f).coerceIn(0f, 0.60f)
    val customTintColor = Color(glassConfig.glassTintColorHex)
    val baseTintColor = if (customTintColor.alpha > 0.01f && glassConfig.glassTintColorHex != 0L) {
        customTintColor.copy(alpha = tintAlpha)
    } else if (glassConfig.isDarkGlass) {
        Color(0xFF141928).copy(alpha = tintAlpha)
    } else {
        Color(0xFFFFFFFF).copy(alpha = tintAlpha)
    }
    val darknessColor = Color.Black.copy(alpha = darkAlpha)

    val edgeFactor = (glassConfig.edgeHighlight.coerceIn(0f, 100f) / 100f)
    val shadowElevation = if (glassConfig.depthEnabled) {
        (glassConfig.shadowElevationDp.takeIf { it > 0f } ?: 8f).coerceIn(0f, 16f).dp
    } else 0.dp

    // Position of dock in root to translate captured feed accurately
    var dockPositionInRoot by remember { mutableStateOf(Offset.Zero) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        val screenWidth = maxWidth
        val barWidth = min(screenWidth * 0.92f, 420.dp)
        val barHeight = 64.dp
        val tabWidth = barWidth / 5

        val barWidthPx = with(density) { barWidth.toPx() }
        val barHeightPx = with(density) { barHeight.toPx() }
        val tabWidthPx = with(density) { tabWidth.toPx() }

        // Interaction state for Lens dragging & spring snap
        var isDragging by remember { mutableStateOf(false) }
        var dragOffsetCumulativePx by remember { mutableFloatStateOf(0f) }
        val lensAnimFraction = remember { Animatable(selectedTab.toFloat()) }

        // Keep animated fraction synced when selectedTab changes outside drag
        LaunchedEffect(selectedTab) {
            if (!isDragging) {
                lensAnimFraction.animateTo(
                    targetValue = selectedTab.toFloat(),
                    animationSpec = spring(
                        dampingRatio = 0.68f, // Slight overshoot
                        stiffness = 300f
                    )
                )
            }
        }

        // Per-tab interaction sources for press-and-hold detection
        val tabInteractionSources = remember { List(5) { MutableInteractionSource() } }
        val isAnyTabPressed = tabInteractionSources.map { it.collectIsPressedAsState().value }
        val isCurrentTabPressed = isAnyTabPressed.getOrElse(selectedTab.coerceIn(0, 4)) { false }

        // Press-and-hold scale animation (iOS 26 Liquid pill grows to 1.15x)
        val lensScale by animateFloatAsState(
            targetValue = if (isCurrentTabPressed || isDragging) 1.15f else 1.0f,
            animationSpec = spring(dampingRatio = 0.68f, stiffness = 300f),
            label = "lens_scale"
        )

        // Fluid stretch/squash during movement
        val currentFraction = lensAnimFraction.value
        val tabDelta = abs(currentFraction - selectedTab.toFloat())
        val stretchX = 1f + (tabDelta * 0.22f).coerceIn(0f, 0.25f)
        val stretchY = 1f - (tabDelta * 0.12f).coerceIn(0f, 0.15f)

        val lensHeight = 54.dp
        val lensWidth = tabWidth - 6.dp
        val lensHeightPx = with(density) { lensHeight.toPx() }
        val lensWidthPx = with(density) { lensWidth.toPx() }
        val lensCornerRadius = (cornerRadius - 4.dp).coerceAtLeast(14.dp)
        val lensShape = RoundedCornerShape(lensCornerRadius)

        val lensOffsetX = (tabWidth * currentFraction) + (tabWidth - lensWidth) / 2
        val lensOffsetXPx = with(density) { lensOffsetX.toPx() }
        val lensOffsetYPx = (barHeightPx - lensHeightPx) / 2f

        // Stronger refraction for lens capsule
        val lensRefraction = (glassConfig.refraction * (1f + glassConfig.lensMagnify / 35f)).coerceIn(0f, 180f)

        Box(
            modifier = Modifier
                .width(barWidth)
                .height(barHeight)
                .onGloballyPositioned { coords ->
                    dockPositionInRoot = coords.positionInRoot()
                }
                .pointerInput(tabWidthPx) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            isDragging = true
                            dragOffsetCumulativePx = 0f
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            dragOffsetCumulativePx += dragAmount
                            val deltaFraction = dragOffsetCumulativePx / tabWidthPx
                            val targetFraction = (selectedTab.toFloat() + deltaFraction).coerceIn(0f, 4f)
                            coroutineScope.launch {
                                lensAnimFraction.snapTo(targetFraction)
                            }
                        },
                        onDragEnd = {
                            isDragging = false
                            val nearestTab = lensAnimFraction.value.roundToInt().coerceIn(0, 4)
                            onTabSelected(nearestTab)
                            coroutineScope.launch {
                                lensAnimFraction.animateTo(
                                    targetValue = nearestTab.toFloat(),
                                    animationSpec = spring(dampingRatio = 0.68f, stiffness = 300f)
                                )
                            }
                        },
                        onDragCancel = {
                            isDragging = false
                            coroutineScope.launch {
                                lensAnimFraction.animateTo(
                                    targetValue = selectedTab.toFloat(),
                                    animationSpec = spring(dampingRatio = 0.68f, stiffness = 300f)
                                )
                            }
                        }
                    )
                }
        ) {
            // =========================================================================
            // SIBLING 1: BASE LIQUID GLASS DOCK SURFACE (NO CHILDREN!)
            // =========================================================================
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (isGlass) {
                            Modifier
                                .then(
                                    if (shadowElevation > 0.dp) {
                                        Modifier.shadow(
                                            elevation = shadowElevation,
                                            shape = dockShape,
                                            ambientColor = Color(0x35000000),
                                            spotColor = Color(0x60000000)
                                        )
                                    } else Modifier
                                )
                                .then(
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        Modifier.graphicsLayer {
                                            if (size.width >= 12f && size.height >= 12f) {
                                                try {
                                                    val shader = com.mytube.ui.glass.LiquidGlassShader.obtain()
                                                    val layerDensity = this.density
                                                    shader.setFloatUniform("resolution", size.width, size.height)
                                                    shader.setFloatUniform("cornerRadius", cornerRadius.value * layerDensity)
                                                    shader.setFloatUniform("refraction", if (isQualityLow) 0f else glassConfig.refraction)
                                                    shader.setFloatUniform("time", (System.currentTimeMillis() % 1000000L) / 1000f)
                                                    shader.setFloatUniform("animSpeed", glassConfig.animSpeed)
                                                    shader.setFloatUniform("edgeHighlight", glassConfig.edgeHighlight)
                                                    shader.setFloatUniform("reflection", glassConfig.reflection)
                                                    shader.setFloatUniform("transparency", glassConfig.transparency)
                                                    shader.setFloatUniform("tintDarkness", glassConfig.tintDarkness)
                                                    val tint = customTintColor
                                                    shader.setFloatUniform("tintColor", floatArrayOf(tint.red, tint.green, tint.blue, 1.0f))
                                                    shader.setFloatUniform("qualityMode", qualityCode)
                                                    shader.setFloatUniform("density", layerDensity)

                                                    val shaderEffect = AndroidRenderEffect.createRuntimeShaderEffect(shader, "background")
                                                    val effectiveBlurPx = (glassConfig.blur * layerDensity * 0.45f).coerceIn(0f, 40f)
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
                                        }
                                    } else Modifier
                                )
                                .clip(dockShape)
                                .drawBehind {
                                    // SAMPLES AND REFRACTS REAL BACKGROUND PASSING UNDER DOCK
                                    if (feedGraphicsLayer != null && feedGraphicsLayer.size.width > 0 && feedGraphicsLayer.size.height > 0) {
                                        translate(left = -dockPositionInRoot.x, top = -dockPositionInRoot.y) {
                                            drawLayer(feedGraphicsLayer)
                                        }
                                    } else {
                                        Log.w("LiquidGlass", "Captured background layer is uninitialized or empty")
                                    }

                                    // Base liquid glass tint fill
                                    drawRect(color = baseTintColor)
                                    // Darkness overlay
                                    if (darkAlpha > 0.01f) {
                                        drawRect(color = darknessColor)
                                    }

                                    // Fallback blur diffusion for pre-API 33
                                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU && glassConfig.blur > 0.5f) {
                                        val blurFactor = (glassConfig.blur / 30f).coerceIn(0.05f, 1f)
                                        drawRect(
                                            brush = Brush.radialGradient(
                                                colors = listOf(
                                                    Color.White.copy(alpha = 0.07f * blurFactor),
                                                    Color(0xFF38BDF8).copy(alpha = 0.03f * blurFactor),
                                                    Color.Transparent
                                                ),
                                                center = Offset(size.width * 0.5f, size.height * 0.5f),
                                                radius = (size.width.coerceAtLeast(size.height)) * 0.85f
                                            )
                                        )
                                    }
                                }
                                .border(
                                    width = 1.dp,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = (edgeFactor * 0.55f).coerceIn(0.12f, 0.85f)),
                                            Color.White.copy(alpha = 0.12f),
                                            Color.White.copy(alpha = (edgeFactor * 0.35f).coerceIn(0.08f, 0.65f))
                                        )
                                    ),
                                    shape = dockShape
                                )
                        } else {
                            Modifier
                                .shadow(elevation = 6.dp, shape = dockShape, spotColor = Color(0x60000000))
                                .clip(dockShape)
                                .background(Color(0xFF1E1E1E).copy(alpha = 0.94f))
                                .border(0.75.dp, Color.White.copy(alpha = 0.18f), dockShape)
                        }
                    )
            )

            // =========================================================================
            // SIBLING 2: LENS CAPSULE (NO CHILDREN!)
            // =========================================================================
            Box(
                modifier = Modifier
                    .offset { IntOffset(lensOffsetXPx.roundToInt(), lensOffsetYPx.roundToInt()) }
                    .width(lensWidth)
                    .height(lensHeight)
                    .graphicsLayer {
                        scaleX = stretchX * lensScale
                        scaleY = stretchY * lensScale
                        alpha = if (lensScale > 1.05f) 0.85f else 1.0f
                    }
                    .then(
                        if (isGlass) {
                            Modifier
                                .shadow(
                                    elevation = 4.dp,
                                    shape = lensShape,
                                    ambientColor = Color(0x35000000),
                                    spotColor = Color(0x50000000)
                                )
                                .then(
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        Modifier.graphicsLayer {
                                            if (size.width >= 10f && size.height >= 10f) {
                                                try {
                                                    val shader = com.mytube.ui.glass.LiquidGlassShader.obtain()
                                                    val layerDensity = this.density
                                                    shader.setFloatUniform("resolution", size.width, size.height)
                                                    shader.setFloatUniform("cornerRadius", lensCornerRadius.value * layerDensity)
                                                    shader.setFloatUniform("refraction", if (isQualityLow) 0f else lensRefraction)
                                                    shader.setFloatUniform("time", (System.currentTimeMillis() % 1000000L) / 1000f)
                                                    shader.setFloatUniform("animSpeed", glassConfig.animSpeed)
                                                    shader.setFloatUniform("edgeHighlight", (glassConfig.edgeHighlight * 1.25f).coerceIn(0f, 100f))
                                                    shader.setFloatUniform("reflection", (glassConfig.reflection * 1.2f).coerceIn(0f, 100f))
                                                    shader.setFloatUniform("transparency", (glassConfig.transparency * 0.95f).coerceIn(10f, 95f))
                                                    shader.setFloatUniform("tintDarkness", glassConfig.tintDarkness)
                                                    val tint = customTintColor
                                                    shader.setFloatUniform("tintColor", floatArrayOf(tint.red, tint.green, tint.blue, 1.0f))
                                                    shader.setFloatUniform("qualityMode", qualityCode)
                                                    shader.setFloatUniform("density", layerDensity)

                                                    val shaderEffect = AndroidRenderEffect.createRuntimeShaderEffect(shader, "background")
                                                    renderEffect = shaderEffect.asComposeRenderEffect()
                                                } catch (_: Exception) {
                                                    renderEffect = null
                                                }
                                            }
                                        }
                                    } else Modifier
                                )
                                .clip(lensShape)
                                .drawBehind {
                                    // SAMPLES UNDERLYING CONTENT WITH LENS TRANSLATION
                                    if (feedGraphicsLayer != null && feedGraphicsLayer.size.width > 0 && feedGraphicsLayer.size.height > 0) {
                                        translate(
                                            left = -(dockPositionInRoot.x + lensOffsetXPx),
                                            top = -(dockPositionInRoot.y + lensOffsetYPx)
                                        ) {
                                            drawLayer(feedGraphicsLayer)
                                        }
                                    } else {
                                        Log.w("LiquidGlass", "Captured background layer is uninitialized or empty")
                                    }

                                    // Translucent lens tint
                                    drawRect(color = Color.White.copy(alpha = 0.08f))
                                    if (darkAlpha > 0.01f) {
                                        drawRect(color = darknessColor)
                                    }
                                }
                                .border(
                                    width = 1.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.85f),
                                            Color.White.copy(alpha = 0.25f),
                                            Color.White.copy(alpha = 0.75f)
                                        )
                                    ),
                                    shape = lensShape
                                )
                        } else {
                            Modifier
                                .clip(lensShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        }
                    )
            )

            // =========================================================================
            // SIBLING 3: FOREGROUND ITEMS (Home, Shorts, Add +, Subscriptions, You)
            // =========================================================================
            // 100% SHARP, ZERO SHADER, ZERO BLUR, ZERO TILT!
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Home
                BottomNavItem(
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_yt_home_outline),
                            contentDescription = "Home",
                            tint = if (selectedTab == 0) activeColor else inactiveColor,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = "Home",
                    isSelected = selectedTab == 0,
                    showLabels = showLabels,
                    testTag = "nav_home",
                    interactionSource = tabInteractionSources[0],
                    onClick = { onTabSelected(0) },
                    modifier = Modifier.width(tabWidth)
                )

                // 2. Shorts
                BottomNavItem(
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_shorts_logo_outline),
                            contentDescription = "Shorts",
                            tint = if (selectedTab == 1) activeColor else inactiveColor,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = "Shorts",
                    isSelected = selectedTab == 1,
                    showLabels = showLabels,
                    testTag = "nav_shorts",
                    interactionSource = tabInteractionSources[1],
                    onClick = { onTabSelected(1) },
                    modifier = Modifier.width(tabWidth)
                )

                // 3. Add (+) Button (Opens local video picker)
                Box(
                    modifier = Modifier
                        .width(tabWidth)
                        .height(barHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isGlass) Color.White.copy(alpha = 0.22f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.65f), CircleShape)
                            .clickable(
                                interactionSource = tabInteractionSources[2],
                                indication = null
                            ) {
                                if (onAddClick != null) {
                                    onAddClick()
                                } else {
                                    onTabSelected(2)
                                }
                            }
                            .testTag("nav_add_plus"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Video / Import",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // 4. Subscriptions
                BottomNavItem(
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_yt_subscriptions_outline),
                            contentDescription = "Subscriptions",
                            tint = if (selectedTab == 3) activeColor else inactiveColor,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = "Subscriptions",
                    isSelected = selectedTab == 3,
                    showLabels = showLabels,
                    testTag = "nav_subscriptions",
                    interactionSource = tabInteractionSources[3],
                    onClick = { onTabSelected(3) },
                    modifier = Modifier.width(tabWidth)
                )

                // 5. You
                BottomNavItem(
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8E24AA))
                                .then(
                                    if (selectedTab == 4) {
                                        Modifier.border(1.5.dp, Color.White, CircleShape)
                                    } else {
                                        Modifier
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "M",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    },
                    label = "You",
                    isSelected = selectedTab == 4,
                    showLabels = showLabels,
                    testTag = "nav_you",
                    interactionSource = tabInteractionSources[4],
                    onClick = { onTabSelected(4) },
                    modifier = Modifier.width(tabWidth)
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: @Composable () -> Unit,
    label: String,
    isSelected: Boolean,
    showLabels: Boolean,
    testTag: String,
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(vertical = 4.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon()
        if (showLabels) {
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.72f),
                maxLines = 1
            )
        }
    }
}
