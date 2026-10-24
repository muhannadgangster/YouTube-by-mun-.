package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.SettingsManager
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassSurface
import com.example.ui.theme.LocalAppAccentColor
import com.example.ui.theme.LocalLiquidGlassConfig
import com.example.ui.theme.YtDarkBackground
import com.example.ui.theme.YtRed
import com.example.ui.theme.YtSurfaceDark
import com.example.ui.theme.YtSurfaceVariant
import com.example.ui.theme.YtTextPrimary
import com.example.ui.theme.YtTextSecondary

/**
 * Polished First-Launch Welcome & Liquid Glass Intro:
 * - Animated translucent Liquid Glass orb with caustic light refraction and smooth breathing motion
 * - Pure offline local player identity
 * - Zero-permission Android Photo Picker for optional custom background wallpaper
 * - Instant Skip option that marks onboarding complete persistently
 */
@Composable
fun FirstLaunchOnboardingScreen(
    settingsManager: SettingsManager,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val accentColor = LocalAppAccentColor.current
    val glassConfig = LocalLiquidGlassConfig.current

    var selectedWallpaperUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedWallpaperUri = uri
        }
    }

    // Infinite gentle animation for the flowing Liquid Glass orb
    val infiniteTransition = rememberInfiniteTransition(label = "glass_orb_anim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_pulse"
    )

    val lightAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orb_light_rotation"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(YtDarkBackground)
    ) {
        // Optional user-selected wallpaper preview behind the screen
        if (selectedWallpaperUri != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(selectedWallpaperUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Selected Wallpaper",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar with Skip button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Skip",
                    color = YtTextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            settingsManager.setOnboardingCompleted(true)
                            onComplete()
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("onboarding_skip_button")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Central Animated Liquid Glass Orb
            Box(
                modifier = Modifier
                    .size(170.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .size(150.dp * pulseScale)
                ) {
                    val radius = size.minDimension / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)

                    // 1. Soft ambient glow behind orb
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.35f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = radius * 1.35f
                        ),
                        radius = radius * 1.35f
                    )

                    // 2. Translucent glass body with water-like refraction
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x35141928),
                                Color(0x601A243D),
                                Color(0x950B101E)
                            ),
                            center = center - Offset(radius * 0.25f, radius * 0.25f),
                            radius = radius * 1.1f
                        ),
                        radius = radius
                    )

                    // 3. Curved specular highlight crescent
                    drawArc(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.85f),
                                Color.White.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        ),
                        startAngle = 180f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(4f, 4f),
                        size = Size(size.width - 8f, size.height - 8f),
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // 4. Subtle inner caustic refraction
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.30f),
                                Color.Transparent
                            ),
                            center = center + Offset(radius * 0.15f, radius * 0.15f),
                            radius = radius * 0.55f
                        ),
                        radius = radius * 0.55f
                    )

                    // 5. Beveled rim outline
                    drawCircle(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.65f),
                                Color.Transparent,
                                Color(0x4038BDF8),
                                Color.White.copy(alpha = 0.65f)
                            ),
                            center = center
                        ),
                        radius = radius,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }

                // Centered App Icon
                Box(
                    modifier = Modifier
                        .size(48.dp, 36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(YtRed)
                        .shadow(8.dp, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // App Identity & Welcome Headline
            Text(
                text = "MyTube",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "OFFLINE MEDIA PLAYER",
                color = accentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Experience your local video library with authentic Liquid Glass optics, rich depth, and YouTube-inspired navigation.",
                color = YtTextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(22.dp))

            // Three Core Features
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FeatureBadge(
                    icon = Icons.Default.Security,
                    title = "100% Offline",
                    subtitle = "No accounts or tracking",
                    modifier = Modifier.weight(1f)
                )
                FeatureBadge(
                    icon = Icons.Default.Diamond,
                    title = "Liquid Glass",
                    subtitle = "Curved optical depth",
                    modifier = Modifier.weight(1f)
                )
                FeatureBadge(
                    icon = Icons.Default.AutoAwesome,
                    title = "PiP & Shorts",
                    subtitle = "Seamless playback",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Wallpaper Selection Card (Screenshot 5 / Onboarding wallpaper picker)
            GlassSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Glass Background Wallpaper",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (selectedWallpaperUri != null) "Photo wallpaper selected ✨" else "Choose a photo to shine through the glass panels",
                                color = YtTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (selectedWallpaperUri != null) {
                        // Wallpaper Preview
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(selectedWallpaperUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Wallpaper Preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.7f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = accentColor, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Selected", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_choose_wallpaper")
                    ) {
                        Text(
                            text = if (selectedWallpaperUri != null) "Change Photo Wallpaper" else "Select Photo from Device",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = "You can change or remove this anytime in Settings.",
                        color = YtTextSecondary,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Continue Button (Applies wallpaper and proceeds)
            Button(
                onClick = {
                    if (selectedWallpaperUri != null) {
                        settingsManager.setCustomBackgroundUri(selectedWallpaperUri.toString())
                    }
                    settingsManager.setOnboardingCompleted(true)
                    onComplete()
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("onboarding_continue_button")
            ) {
                Text(
                    text = "Continue to MyTube",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Skip / Use Default Button
            Text(
                text = "Use Default Theme & Skip",
                color = YtTextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        settingsManager.setOnboardingCompleted(true)
                        onComplete()
                    }
                    .padding(8.dp)
                    .testTag("onboarding_bottom_skip")
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun FeatureBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(YtSurfaceDark)
            .border(0.75.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                color = YtTextSecondary,
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )
        }
    }
}
