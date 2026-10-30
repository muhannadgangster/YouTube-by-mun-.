package com.example.launcher.ui.settings

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.launcher.data.LauncherPreferencesManager
import com.example.launcher.glass.LiquidGlassSurface
import com.example.launcher.model.GlassSettings
import com.example.launcher.model.HomeScreenConfig
import com.example.launcher.wallpaper.WallpaperProvider
import kotlinx.coroutines.launch

@Composable
fun LauncherSettingsScreen(
    config: HomeScreenConfig,
    glassSettings: GlassSettings,
    preferencesManager: LauncherPreferencesManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showResetDialog by remember { mutableStateOf(false) }

    // Role Manager request launcher for Home app role
    val roleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { /* Refreshes naturally */ }

    // Custom Wallpaper Picker launcher
    val wallpaperPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                preferencesManager.updateHomeConfig(
                    config.copy(
                        wallpaperId = "custom",
                        customWallpaperUri = uri.toString()
                    )
                )
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F12))
            .statusBarsPadding()
    ) {
        // iOS Navigation Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF60A5FA),
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = "Settings",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Profile & Default Launcher
            item {
                SettingsSectionHeader("Default Home App")
                LiquidGlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    cornerRadius = 18.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3B82F6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "iOS 27 Launcher",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Production Home Screen Replacement",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
                                    if (roleManager?.isRoleAvailable(RoleManager.ROLE_HOME) == true) {
                                        val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                                        roleLauncher.launch(intent)
                                    } else {
                                        context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                    }
                                } else {
                                    context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Set as Default Launcher", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Section 2: Real-time Liquid Glass Engine
            item {
                SettingsSectionHeader("Liquid Glass Studio")
                LiquidGlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    cornerRadius = 18.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SettingsToggleRow(
                            title = "Liquid Glass Effect",
                            subtitle = "Real optical translucency, bevel & specular rim",
                            isChecked = glassSettings.isEnabled,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    preferencesManager.updateGlassSettings(glassSettings.copy(isEnabled = it))
                                }
                            }
                        )

                        if (glassSettings.isEnabled) {
                            Spacer(modifier = Modifier.height(14.dp))
                            SettingsSliderRow(
                                title = "Transparency",
                                value = glassSettings.transparency,
                                range = 0.05f..0.85f,
                                onValueChange = {
                                    coroutineScope.launch {
                                        preferencesManager.updateGlassSettings(glassSettings.copy(transparency = it))
                                    }
                                }
                            )

                            SettingsSliderRow(
                                title = "Blur Intensity (Radius)",
                                value = glassSettings.blurRadiusDp,
                                range = 4f..48f,
                                onValueChange = {
                                    coroutineScope.launch {
                                        preferencesManager.updateGlassSettings(glassSettings.copy(blurRadiusDp = it))
                                    }
                                }
                            )

                            SettingsSliderRow(
                                title = "Edge Highlight & Bevel",
                                value = glassSettings.edgeHighlightStrength,
                                range = 10f..100f,
                                onValueChange = {
                                    coroutineScope.launch {
                                        preferencesManager.updateGlassSettings(glassSettings.copy(edgeHighlightStrength = it))
                                    }
                                }
                            )

                            SettingsSliderRow(
                                title = "Diagonal Reflection Sheen",
                                value = glassSettings.reflectionSheen,
                                range = 0f..100f,
                                onValueChange = {
                                    coroutineScope.launch {
                                        preferencesManager.updateGlassSettings(glassSettings.copy(reflectionSheen = it))
                                    }
                                }
                            )

                            SettingsSliderRow(
                                title = "Glass Corner Radius",
                                value = glassSettings.cornerRadiusDp,
                                range = 10f..36f,
                                onValueChange = {
                                    coroutineScope.launch {
                                        preferencesManager.updateGlassSettings(glassSettings.copy(cornerRadiusDp = it))
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Performance Quality",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                listOf("HIGH" to "High", "MEDIUM" to "Balanced", "LOW" to "Eco (Go)").forEach { (mode, label) ->
                                    val isSelected = glassSettings.performanceMode == mode
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) Color(0xFF2563EB) else Color.White.copy(alpha = 0.1f))
                                            .clickable {
                                                coroutineScope.launch {
                                                    preferencesManager.updateGlassSettings(glassSettings.copy(performanceMode = mode))
                                                }
                                            }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            TextButton(
                                onClick = {
                                    coroutineScope.launch {
                                        preferencesManager.updateGlassSettings(GlassSettings())
                                    }
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF60A5FA))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reset to iOS Glass Look", color = Color(0xFF60A5FA), fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Section 3: Wallpaper & Appearance
            item {
                SettingsSectionHeader("Wallpapers & Theme")
                LiquidGlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    cornerRadius = 18.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Bundled Offline Wallpapers",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            WallpaperProvider.bundledWallpapers.forEach { wp ->
                                val isSelected = config.wallpaperId == wp.id && config.customWallpaperUri == null
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(wp.previewColor)
                                        .then(
                                            if (isSelected) Modifier.border(2.5.dp, Color.White, CircleShape)
                                            else Modifier
                                        )
                                        .clickable {
                                            coroutineScope.launch {
                                                preferencesManager.updateHomeConfig(
                                                    config.copy(
                                                        wallpaperId = wp.id,
                                                        customWallpaperUri = null
                                                    )
                                                )
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                wallpaperPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Wallpaper, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Choose from Photo Gallery", color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        SettingsSliderRow(
                            title = "Wallpaper Dimming Scrim",
                            value = config.wallpaperDim,
                            range = 0.0f..0.60f,
                            onValueChange = {
                                coroutineScope.launch {
                                    preferencesManager.updateHomeConfig(config.copy(wallpaperDim = it))
                                }
                            }
                        )

                        SettingsSliderRow(
                            title = "Wallpaper Blur",
                            value = config.wallpaperBlurDp,
                            range = 0.0f..30.0f,
                            onValueChange = {
                                coroutineScope.launch {
                                    preferencesManager.updateHomeConfig(config.copy(wallpaperBlurDp = it))
                                }
                            }
                        )
                    }
                }
            }

            // Section 4: Home Screen Grid & Icons
            item {
                SettingsSectionHeader("Home Screen & Dock Grid")
                LiquidGlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    cornerRadius = 18.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SettingsToggleRow(
                            title = "Show App Labels",
                            subtitle = "Clean text labels under home icons",
                            isChecked = config.showLabels,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    preferencesManager.updateHomeConfig(config.copy(showLabels = it))
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        SettingsSliderRow(
                            title = "Icon Scale",
                            value = config.iconSizeScale,
                            range = 0.8f..1.25f,
                            onValueChange = {
                                coroutineScope.launch {
                                    preferencesManager.updateHomeConfig(config.copy(iconSizeScale = it))
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Icon Shape",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("SQUIRCLE" to "iOS Squircle", "CIRCLE" to "Circle", "ROUNDED_SQUARE" to "Rounded").forEach { (shape, label) ->
                                val isSelected = config.iconShape == shape
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) Color(0xFF2563EB) else Color.White.copy(alpha = 0.1f))
                                        .clickable {
                                            coroutineScope.launch {
                                                preferencesManager.updateHomeConfig(config.copy(iconShape = shape))
                                            }
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 5: Gestures & Lock Screen Typography
            item {
                SettingsSectionHeader("Lock Screen & Gestures")
                LiquidGlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    cornerRadius = 18.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Clock Typography Style",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("SF_BOLD" to "SF Bold", "SF_THIN" to "Light", "SERIF" to "Serif", "ROUNDED" to "Rounded").forEach { (style, label) ->
                                val isSelected = config.clockStyle == style
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) Color(0xFF2563EB) else Color.White.copy(alpha = 0.1f))
                                        .clickable {
                                            coroutineScope.launch {
                                                preferencesManager.updateHomeConfig(config.copy(clockStyle = style))
                                            }
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        SettingsToggleRow(
                            title = "Haptic Tactile Feedback",
                            subtitle = "Vibrate on long press and toggles",
                            isChecked = config.hapticFeedback,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    preferencesManager.updateHomeConfig(config.copy(hapticFeedback = it))
                                }
                            }
                        )

                        SettingsToggleRow(
                            title = "Reduced Motion",
                            subtitle = "Smooth fade transitions instead of motion",
                            isChecked = config.reducedMotion,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    preferencesManager.updateHomeConfig(config.copy(reducedMotion = it))
                                }
                            }
                        )
                    }
                }
            }

            // Section 6: Privacy & Reset
            item {
                SettingsSectionHeader("Privacy & Reset")
                LiquidGlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    cornerRadius = 18.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("100% Offline & Private Guarantee", color = Color(0xFF10B981), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Zero internet permissions. All customized icons, wallpapers, layout orders, and settings are stored locally on your device.",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { showResetDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reset All Launcher Settings to Default")
                        }
                    }
                }
            }

            // About
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "iOS 27 Launcher for Android",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Version 1.0.0 (Production Release)",
                        color = Color.White.copy(alpha = 0.35f),
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset All Settings?") },
            text = { Text("This will restore default home screen layouts, dock apps, wallpapers, and liquid glass settings. Your installed apps will remain intact.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            preferencesManager.resetToDefaults()
                            showResetDialog = false
                        }
                    }
                ) {
                    Text("Reset Everything", color = Color(0xFFEF4444))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        color = Color.White.copy(alpha = 0.5f),
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 6.dp, bottom = 6.dp)
    )
}

@Composable
fun SettingsToggleRow(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Color.White.copy(alpha = 0.5f), fontSize = 11.5.sp)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF2563EB)
            )
        )
    }
}

@Composable
fun SettingsSliderRow(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, color = Color.White, fontSize = 13.5.sp)
            Text(String.format("%.1f", value), color = Color(0xFF60A5FA), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF3B82F6),
                activeTrackColor = Color(0xFF3B82F6)
            )
        )
    }
}
