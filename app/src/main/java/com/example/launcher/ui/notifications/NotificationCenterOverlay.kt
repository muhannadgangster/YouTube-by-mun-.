package com.example.launcher.ui.notifications

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.launcher.glass.LiquidGlassSurface
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationCenterOverlay(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        val context = LocalContext.current
        var currentTime by remember { mutableStateOf("") }
        var currentDate by remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
            while (true) {
                val now = Date()
                currentTime = SimpleDateFormat("h:mm", Locale.getDefault()).format(now)
                currentDate = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(now)
                delay(1000)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.60f))
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
                    .padding(horizontal = 20.dp, vertical = 18.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Clock
                Text(
                    text = currentTime,
                    color = Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1.5).sp
                )
                Text(
                    text = currentDate,
                    color = Color(0xFF60A5FA),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Notification Center Privacy Card
                LiquidGlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    cornerRadius = 22.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3B82F6).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = Color(0xFF60A5FA),
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Notification Center",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Your device notifications are private and remain 100% on your device. Android system notifications can be managed anytime via system settings.",
                            color = Color.White.copy(alpha = 0.70f),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                try {
                                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                } catch (_: Exception) {
                                    try {
                                        context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                    } catch (_: Exception) {}
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2563EB)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Manage Notifications", fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Dismiss bar handle
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.4f))
                        .clickable { onDismiss() }
                )
            }
        }
    }
}
