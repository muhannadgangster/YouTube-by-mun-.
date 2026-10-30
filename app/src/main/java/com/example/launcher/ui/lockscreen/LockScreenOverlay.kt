package com.example.launcher.ui.lockscreen

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.MediaStore
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.launcher.glass.LiquidGlassSurface
import com.example.launcher.model.HomeScreenConfig
import com.example.launcher.wallpaper.WallpaperBackground
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun LockScreenOverlay(
    isVisible: Boolean,
    config: HomeScreenConfig,
    onUnlock: () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        val context = LocalContext.current
        var currentTime by remember { mutableStateOf("") }
        var currentDate by remember { mutableStateOf("") }
        var isTorchOn by remember { mutableStateOf(false) }

        // Live clock
        LaunchedEffect(Unit) {
            while (true) {
                val now = Date()
                currentTime = SimpleDateFormat("h:mm", Locale.getDefault()).format(now)
                currentDate = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(now)
                delay(1000)
            }
        }

        // Camera manager for real flashlight toggle
        val cameraManager = remember { context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager }
        val cameraId = remember {
            try {
                cameraManager?.cameraIdList?.firstOrNull()
            } catch (_: Exception) {
                null
            }
        }
        val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }

        fun toggleTorch() {
            if (cameraManager != null && cameraId != null) {
                try {
                    val next = !isTorchOn
                    cameraManager.setTorchMode(cameraId, next)
                    isTorchOn = next
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                } catch (_: Exception) {}
            }
        }

        // Drag to unlock state
        var offsetY by remember { mutableFloatStateOf(0f) }
        val draggableState = rememberDraggableState { delta ->
            val newOffset = (offsetY + delta).coerceAtMost(0f)
            offsetY = newOffset
            if (offsetY < -240f) {
                onUnlock()
                offsetY = 0f
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, offsetY.roundToInt()) }
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Vertical,
                    onDragStopped = {
                        if (offsetY < -150f) {
                            onUnlock()
                        }
                        offsetY = 0f
                    }
                )
        ) {
            // High-resolution Wallpaper
            WallpaperBackground(config = config)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top section: Lock icon + Date + iOS Depth Clock
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = currentDate,
                        color = Color.White.copy(alpha = 0.90f),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.8f),
                                offset = Offset(0f, 2f),
                                blurRadius = 4f
                            )
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val clockFont = when (config.clockStyle) {
                        "SF_THIN" -> FontWeight.Light
                        "SERIF" -> FontWeight.Normal
                        "ROUNDED" -> FontWeight.Medium
                        else -> FontWeight.Bold
                    }
                    val fontFamily = if (config.clockStyle == "SERIF") FontFamily.Serif else FontFamily.Default

                    Text(
                        text = currentTime,
                        color = Color.White,
                        fontSize = 82.sp,
                        fontWeight = clockFont,
                        fontFamily = fontFamily,
                        letterSpacing = (-2.5).sp,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.65f),
                                offset = Offset(0f, 4f),
                                blurRadius = 8f
                            )
                        )
                    )

                    // Lock Screen Mini Widgets Row
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LiquidGlassSurface(
                            modifier = Modifier
                                .height(38.dp)
                                .padding(horizontal = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            cornerRadius = 12.dp
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {
                                Text(
                                    text = "72° Mostly Sunny",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        LiquidGlassSurface(
                            modifier = Modifier
                                .height(38.dp)
                                .padding(horizontal = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            cornerRadius = 12.dp
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {
                                Text(
                                    text = "🔋 100% Charged",
                                    color = Color(0xFF10B981),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Bottom section: Quick action buttons + "Swipe up to open"
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Torch button
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isTorchOn) Color(0xFF3B82F6) else Color.Black.copy(alpha = 0.45f)
                                )
                                .clickable { toggleTorch() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashlightOn,
                                contentDescription = "Torch",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Camera button
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.45f))
                                .clickable {
                                    try {
                                        context.startActivity(Intent(MediaStore.ACTION_IMAGE_CAPTURE).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                    } catch (_: Exception) {}
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Camera",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = "Swipe up to unlock",
                        color = Color.White.copy(alpha = 0.70f),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.9f),
                                offset = Offset(0f, 1f),
                                blurRadius = 3f
                            )
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Home indicator bar at bottom
                    Box(
                        modifier = Modifier
                            .width(134.dp)
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.85f))
                            .clickable { onUnlock() }
                    )
                }
            }
        }
    }
}
