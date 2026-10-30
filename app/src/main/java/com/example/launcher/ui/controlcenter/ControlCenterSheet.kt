package com.example.launcher.ui.controlcenter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NetworkCell
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.launcher.glass.LiquidGlassSurface

@Composable
fun ControlCenterOverlay(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        val context = LocalContext.current
        val activity = context as? Activity

        // Real Camera Flashlight / Torch Manager
        var isTorchOn by remember { mutableStateOf(false) }
        val cameraManager = remember { context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager }
        val cameraId = remember {
            try {
                cameraManager?.cameraIdList?.firstOrNull()
            } catch (_: Exception) {
                null
            }
        }

        fun toggleTorch() {
            if (cameraManager != null && cameraId != null) {
                try {
                    val next = !isTorchOn
                    cameraManager.setTorchMode(cameraId, next)
                    isTorchOn = next
                } catch (_: Exception) {}
            }
        }

        // Real AudioManager for System Volume Slider
        val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }
        val maxVolume = remember { audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15 }
        var currentVolumeFraction by remember {
            val curr = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: (maxVolume / 2)
            mutableFloatStateOf(curr.toFloat() / maxVolume)
        }

        // Real Window Screen Brightness Slider
        var currentBrightnessFraction by remember {
            val lp = activity?.window?.attributes
            val cur = lp?.screenBrightness ?: 0.6f
            mutableFloatStateOf(if (cur < 0) 0.6f else cur)
        }

        // Real Media Playing State
        var isPlayingMedia by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Consume inner click
                    )
            ) {
                // Top Row: 2x2 Network Pod + 2x2 Media Pod
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Connectivity 2x2 Pod
                    LiquidGlassSurface(
                        modifier = Modifier
                            .weight(1f)
                            .height(156.dp),
                        shape = RoundedCornerShape(26.dp),
                        cornerRadius = 26.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                ControlCenterIconButton(
                                    icon = Icons.Default.AirplanemodeActive,
                                    label = "Airplane",
                                    isActive = false,
                                    activeColor = Color(0xFFF97316),
                                    onClick = {
                                        try {
                                            context.startActivity(Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                        } catch (_: Exception) {}
                                    }
                                )
                                ControlCenterIconButton(
                                    icon = Icons.Default.NetworkCell,
                                    label = "Cellular",
                                    isActive = true,
                                    activeColor = Color(0xFF10B981),
                                    onClick = {
                                        try {
                                            context.startActivity(Intent(Settings.ACTION_DATA_ROAMING_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                        } catch (_: Exception) {}
                                    }
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                ControlCenterIconButton(
                                    icon = Icons.Default.Wifi,
                                    label = "Wi-Fi",
                                    isActive = true,
                                    activeColor = Color(0xFF3B82F6),
                                    onClick = {
                                        try {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                context.startActivity(Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                            } else {
                                                context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                            }
                                        } catch (_: Exception) {}
                                    }
                                )
                                ControlCenterIconButton(
                                    icon = Icons.Default.Bluetooth,
                                    label = "Bluetooth",
                                    isActive = true,
                                    activeColor = Color(0xFF3B82F6),
                                    onClick = {
                                        try {
                                            context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                        } catch (_: Exception) {}
                                    }
                                )
                            }
                        }
                    }

                    // Media Player 2x2 Pod
                    LiquidGlassSurface(
                        modifier = Modifier
                            .weight(1f)
                            .height(156.dp),
                        shape = RoundedCornerShape(26.dp),
                        cornerRadius = 26.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Now Playing",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (isPlayingMedia) "Offline Audio" else "Not Playing",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "iPhone Music",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 11.5.sp
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = "Prev",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable {}
                                )
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.20f))
                                        .clickable { isPlayingMedia = !isPlayingMedia },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play/Pause",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Next",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable {}
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Middle Row: Screen Rotation, Dark Mode, Brightness Slider, Volume Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left 2x2 column: Rotation lock & Theme mode
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LiquidGlassSurface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp)
                                .clickable {
                                    try {
                                        context.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                    } catch (_: Exception) {}
                                },
                            shape = RoundedCornerShape(20.dp),
                            cornerRadius = 20.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ScreenRotation,
                                    contentDescription = "Rotation",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Portrait Lock",
                                    color = Color.White,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        LiquidGlassSurface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp)
                                .clickable { onToggleDarkMode() },
                            shape = RoundedCornerShape(20.dp),
                            cornerRadius = 20.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = "Dark Mode",
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (isDarkMode) "Dark Mode" else "Light Mode",
                                    color = Color.White,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Brightness Vertical Capsule Slider
                    VerticalCapsuleSlider(
                        value = currentBrightnessFraction,
                        icon = Icons.Default.BrightnessLow,
                        label = "Brightness",
                        onValueChange = { frac ->
                            currentBrightnessFraction = frac
                            val lp = activity?.window?.attributes
                            if (lp != null) {
                                lp.screenBrightness = frac.coerceIn(0.05f, 1.0f)
                                activity.window.attributes = lp
                            }
                        },
                        modifier = Modifier
                            .weight(0.5f)
                            .height(152.dp)
                    )

                    // Volume Vertical Capsule Slider
                    VerticalCapsuleSlider(
                        value = currentVolumeFraction,
                        icon = Icons.Default.VolumeUp,
                        label = "Volume",
                        onValueChange = { frac ->
                            currentVolumeFraction = frac
                            val targetVol = (frac * maxVolume).toInt()
                            audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, 0)
                        },
                        modifier = Modifier
                            .weight(0.5f)
                            .height(152.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Quick Action Buttons: Flashlight, Calculator, Camera, Low Power
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SquareGlassActionButton(
                        icon = Icons.Default.FlashlightOn,
                        isActive = isTorchOn,
                        activeColor = Color(0xFF3B82F6),
                        onClick = { toggleTorch() }
                    )
                    SquareGlassActionButton(
                        icon = Icons.Default.Calculate,
                        isActive = false,
                        onClick = {
                            try {
                                val intent = Intent().setAction(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALCULATOR)
                                context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            } catch (_: Exception) {
                                try {
                                    context.startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage("com.google.android.calculator").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                } catch (_: Exception) {}
                            }
                        }
                    )
                    SquareGlassActionButton(
                        icon = Icons.Default.CameraAlt,
                        isActive = false,
                        onClick = {
                            try {
                                context.startActivity(Intent(MediaStore.ACTION_IMAGE_CAPTURE).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            } catch (_: Exception) {}
                        }
                    )
                    SquareGlassActionButton(
                        icon = Icons.Default.BatteryChargingFull,
                        isActive = false,
                        activeColor = Color(0xFFF59E0B),
                        onClick = {
                            try {
                                context.startActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            } catch (_: Exception) {}
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Handle bar at bottom to dismiss
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(42.dp)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.4f))
                        .clickable { onDismiss() }
                )
            }
        }
    }
}

@Composable
fun VerticalCapsuleSlider(
    value: Float,
    icon: ImageVector,
    label: String,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    LiquidGlassSurface(
        modifier = modifier
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, _ ->
                    val y = change.position.y
                    val h = size.height
                    val fraction = (1f - (y / h)).coerceIn(0f, 1f)
                    onValueChange(fraction)
                }
            },
        shape = RoundedCornerShape(22.dp),
        cornerRadius = 22.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Fill level from bottom
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(value.coerceIn(0f, 1f))
                    .background(Color.White.copy(alpha = 0.28f))
            )
            // Icon at bottom
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .size(24.dp)
            )
        }
    }
}

@Composable
fun ControlCenterIconButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color = Color(0xFF3B82F6),
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(if (isActive) activeColor else Color.White.copy(alpha = 0.12f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
fun SquareGlassActionButton(
    icon: ImageVector,
    isActive: Boolean,
    activeColor: Color = Color(0xFF3B82F6),
    onClick: () -> Unit
) {
    LiquidGlassSurface(
        modifier = Modifier
            .size(72.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        cornerRadius = 22.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isActive) Modifier.background(activeColor.copy(alpha = 0.50f))
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
