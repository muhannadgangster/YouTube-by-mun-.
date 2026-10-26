package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.LiquidPhysics
import com.example.ui.theme.LocalAppAccentColor
import com.example.ui.theme.LocalLiquidGlassConfig
import com.example.ui.theme.LocalUiCustomization
import com.example.ui.theme.liquidGlassSurface
import kotlin.math.abs

/**
 * YouTube Navigation Bar with exact iOS Liquid Glass floating pill structure:
 * - Floating pill: width min(92% of screen, 420dp), height 64dp, 12dp above bottom inset
 * - Corner radius = Corner radius setting
 * - 5 items: Home, Shorts, Add (+ opens local video picker), Subscriptions, You
 * - Separate LENS capsule behind selected item (height 54dp, same width as one item):
 *   own glass layer with STRONGER refraction = Refraction * (1 + LensMagnify / 35).
 *   On tab change it slides with a spring that overshoots slightly, and stretches
 *   during move (scale about 1.22 x 0.88, then settles).
 * - Real background visible through the glass; foreground icons and labels 100% razor sharp!
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
    val isGlass = glassConfig.isEnabled && customization.navBarGlassEnabled
    val activeColor = if (accentColor != Color.Unspecified && accentColor != Color.Transparent) accentColor else Color.White
    val inactiveColor = Color.White.copy(alpha = 0.72f)
    val showLabels = customization.navBarShowLabels

    // Floating pill geometry: corner radius from setting (10..32 dp)
    val cornerRadius = glassConfig.cornerRadiusDp.dp.coerceIn(10.dp, 32.dp)
    val dockShape = RoundedCornerShape(cornerRadius)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp), // 12dp above the bottom inset
        contentAlignment = Alignment.Center
    ) {
        val screenWidth = maxWidth
        // Pill width: min(92% of screen, 420dp), height 64dp
        val barWidth = min(screenWidth * 0.92f, 420.dp)
        val barHeight = 64.dp

        Box(
            modifier = Modifier
                .width(barWidth)
                .height(barHeight)
        ) {
            // LAYER 1: BASE LIQUID GLASS DOCK SURFACE
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (isGlass) {
                            Modifier
                                .liquidGlassSurface(
                                    shape = dockShape,
                                    config = glassConfig,
                                    accentColor = accentColor,
                                    elevation = 8.dp
                                )
                                .border(
                                    width = 1.dp,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.55f),
                                            Color.White.copy(alpha = 0.12f),
                                            Color.White.copy(alpha = 0.35f)
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

            // LAYER 2: LENS CAPSULE (Separate glass layer behind selected item)
            // Height 54dp, stronger refraction = Refraction * (1 + LensMagnify / 35)
            // Slides with spring that overshoots slightly, stretches during move (~1.22 x 0.88), settles
            val tabWidth = barWidth / 5
            val targetTabIndex = selectedTab.coerceIn(0, 4)

            val animatedTabFraction by animateFloatAsState(
                targetValue = targetTabIndex.toFloat(),
                animationSpec = LiquidPhysics.NavigationSpring,
                label = "lens_tab_slide"
            )

            val tabDelta = abs(targetTabIndex.toFloat() - animatedTabFraction)
            // Stretch during movement (scale ~ 1.22 x 0.88)
            val stretchX = 1f + (tabDelta * 0.22f).coerceIn(0f, 0.25f)
            val stretchY = 1f - (tabDelta * 0.12f).coerceIn(0f, 0.15f)

            val lensHeight = 54.dp
            val lensWidth = tabWidth - 6.dp
            val lensCornerRadius = (cornerRadius - 4.dp).coerceAtLeast(14.dp)
            val lensShape = RoundedCornerShape(lensCornerRadius)

            // Separate LENS glass config with stronger optical displacement
            val lensGlassConfig = remember(glassConfig) {
                val strongRefraction = (glassConfig.refraction * (1f + glassConfig.lensMagnify / 35f)).coerceIn(0f, 180f)
                glassConfig.copy(
                    refraction = strongRefraction,
                    edgeHighlight = (glassConfig.edgeHighlight * 1.25f).coerceIn(0f, 100f),
                    transparency = (glassConfig.transparency * 0.95f).coerceIn(10f, 95f),
                    cornerRadiusDp = lensCornerRadius.value,
                    reflection = (glassConfig.reflection * 1.2f).coerceIn(0f, 100f)
                )
            }

            Box(
                modifier = Modifier
                    .offset(x = tabWidth * animatedTabFraction + (tabWidth - lensWidth) / 2)
                    .width(lensWidth)
                    .height(lensHeight)
                    .align(Alignment.CenterStart)
                    .graphicsLayer {
                        scaleX = stretchX
                        scaleY = stretchY
                    }
                    .then(
                        if (isGlass) {
                            Modifier
                                .liquidGlassSurface(
                                    shape = lensShape,
                                    config = lensGlassConfig,
                                    accentColor = accentColor,
                                    elevation = 3.dp
                                )
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

            // LAYER 3: FOREGROUND ITEMS (Home, Shorts, Add +, Subscriptions, You)
            // Razor sharp, never distorted or blurred!
            Row(
                modifier = Modifier
                    .fillMaxSize(),
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
                            .then(
                                if (isGlass) {
                                    Modifier.liquidGlassSurface(
                                        shape = CircleShape,
                                        config = glassConfig,
                                        accentColor = accentColor,
                                        elevation = 4.dp
                                    )
                                } else {
                                    Modifier
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                }
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
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
