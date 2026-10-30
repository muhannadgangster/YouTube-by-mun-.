package com.example.launcher.ui.widgets

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import android.provider.AlarmClock
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.launcher.glass.LiquidGlassSurface
import com.example.launcher.model.WidgetType
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LauncherWidgetCard(
    widgetType: WidgetType,
    onQuickActionClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LiquidGlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .height(155.dp),
        shape = RoundedCornerShape(26.dp),
        cornerRadius = 26.dp
    ) {
        when (widgetType) {
            WidgetType.CLOCK_BATTERY -> ClockBatteryWidgetContent()
            WidgetType.WEATHER -> WeatherWidgetContent()
            WidgetType.QUICK_ACTIONS -> QuickActionsWidgetContent(onQuickActionClick)
            WidgetType.SYSTEM_STATS -> SystemStatsWidgetContent()
        }
    }
}

@Composable
fun ClockBatteryWidgetContent() {
    val context = LocalContext.current
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }
    var batteryPct by remember { mutableFloatStateOf(100f) }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            currentTime = SimpleDateFormat("h:mm", Locale.getDefault()).format(now)
            currentDate = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(now)

            // Read live battery
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            if (bm != null) {
                val pct = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                batteryPct = pct.toFloat()
            }
            delay(1000)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = currentTime,
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1.5).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = currentDate,
                color = Color(0xFF60A5FA),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "iOS 27 Launcher",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp
            )
        }

        // Circular battery ring
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { batteryPct / 100f },
                    modifier = Modifier.size(56.dp),
                    color = if (batteryPct > 20f) Color(0xFF10B981) else Color(0xFFEF4444),
                    trackColor = Color.White.copy(alpha = 0.15f),
                    strokeWidth = 6.dp
                )
                Text(
                    text = "${batteryPct.toInt()}%",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Battery",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun WeatherWidgetContent() {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Cupertino",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "72°",
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Light
            )
            Text(
                text = "Mostly Sunny",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.WbSunny,
                contentDescription = "Weather",
                tint = Color(0xFFFBBF24),
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "H: 78°  L: 58°",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
            Text(
                text = "AQI: 32 • Good",
                color = Color(0xFF34D399),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun QuickActionsWidgetContent(onActionClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Text(
            text = "Quick Shortcuts",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            QuickActionButton(
                icon = Icons.Default.FlashlightOn,
                label = "Flashlight",
                tint = Color(0xFFF59E0B),
                onClick = { onActionClick("torch") }
            )
            QuickActionButton(
                icon = Icons.Default.Calculate,
                label = "Calculator",
                tint = Color(0xFFF97316),
                onClick = { onActionClick("calculator") }
            )
            QuickActionButton(
                icon = Icons.Default.Search,
                label = "Spotlight",
                tint = Color(0xFF3B82F6),
                onClick = { onActionClick("search") }
            )
            QuickActionButton(
                icon = Icons.Default.CameraAlt,
                label = "Camera",
                tint = Color(0xFF10B981),
                onClick = { onActionClick("camera") }
            )
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.20f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 10.5.sp
        )
    }
}

@Composable
fun SystemStatsWidgetContent() {
    val context = LocalContext.current
    var availableStorageGb by remember { mutableFloatStateOf(0f) }
    var totalStorageGb by remember { mutableFloatStateOf(0f) }
    var availableRamMb by remember { mutableFloatStateOf(0f) }
    var totalRamMb by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        val stat = StatFs(Environment.getDataDirectory().path)
        val bytesAvailable = stat.availableBlocksLong * stat.blockSizeLong
        val bytesTotal = stat.blockCountLong * stat.blockSizeLong
        availableStorageGb = bytesAvailable / (1024f * 1024f * 1024f)
        totalStorageGb = bytesTotal / (1024f * 1024f * 1024f)

        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)
        availableRamMb = memInfo.availMem / (1024f * 1024f)
        totalRamMb = memInfo.totalMem / (1024f * 1024f)
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "System Health",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Storage Available",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp
            )
            Text(
                text = String.format(Locale.US, "%.1f GB of %.0f GB", availableStorageGb, totalStorageGb),
                color = Color(0xFF60A5FA),
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "RAM Available",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp
            )
            Text(
                text = String.format(Locale.US, "%.0f MB of %.0f MB", availableRamMb, totalRamMb),
                color = Color(0xFF34D399),
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            val usedStorageRatio = if (totalStorageGb > 0) ((totalStorageGb - availableStorageGb) / totalStorageGb) else 0f
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { usedStorageRatio },
                    modifier = Modifier.size(54.dp),
                    color = Color(0xFF8B5CF6),
                    trackColor = Color.White.copy(alpha = 0.15f),
                    strokeWidth = 6.dp
                )
                Text(
                    text = "${(usedStorageRatio * 100).toInt()}%",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Storage Used",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 10.sp
            )
        }
    }
}
