package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SettingsManager
import com.example.data.model.ShortsActionConfig
import com.example.data.model.ShortsActionDefaults
import com.example.ui.components.LiquidGlassSubscribeButton
import com.example.ui.theme.LocalAppAccentColor
import com.example.ui.theme.LocalLiquidGlassConfig
import com.example.ui.theme.YtAvatarTeal
import com.example.ui.theme.YtDarkBackground
import com.example.ui.theme.YtRed
import com.example.ui.theme.liquidGlassSurface
import kotlin.math.roundToInt

/**
 * Dedicated Real-Time Shorts Action Icon Position Editor:
 * - Accessible EXCLUSIVELY via Settings → Customize Interface → Shorts → Demo Shorts Editor
 * - Displays a live interactive Demo Shorts player preview
 * - Long-press an icon (approx. 3-5 seconds) to unlock drag mode
 * - Real-time repositioning with normalized coordinates
 * - Drag to marked removal area (Trash zone) to hide/remove an icon
 * - Plus (+) button to restore hidden icons
 * - Reset to Default and Save/Done controls
 * - Prevents removal of critical playback and accessibility controls
 */
@Composable
fun DemoShortsEditorScreen(
    settingsManager: SettingsManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val accentColor = LocalAppAccentColor.current
    val glassConfig = LocalLiquidGlassConfig.current

    val actionConfigs by settingsManager.shortsActionConfigs.collectAsStateWithLifecycle()
    var localConfigs by remember(actionConfigs) { mutableStateOf(actionConfigs) }

    var draggedActionId by remember { mutableStateOf<String?>(null) }
    var isDraggingOverTrash by remember { mutableStateOf(false) }
    var showAddIconDialog by remember { mutableStateOf(false) }

    BackHandler {
        settingsManager.saveShortsActionConfigs(localConfigs)
        onBack()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YtDarkBackground)
            .statusBarsPadding()
    ) {
        // Top Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    settingsManager.saveShortsActionConfigs(localConfigs)
                    onBack()
                },
                modifier = Modifier.testTag("demo_shorts_back")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Demo Shorts Editor",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Text(
                    text = "Long-press 3s to drag icons • Drag to trash to hide",
                    color = accentColor,
                    fontSize = 11.sp
                )
            }

            // Reset to Default Button
            IconButton(
                onClick = {
                    localConfigs = ShortsActionDefaults.getDefaultActions()
                    settingsManager.saveShortsActionConfigs(localConfigs)
                    Toast.makeText(context, "Shorts icons reset to default", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.testTag("demo_shorts_reset")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    tint = Color.White
                )
            }

            // Add (+) Icon Button
            IconButton(
                onClick = { showAddIconDialog = true },
                modifier = Modifier.testTag("demo_shorts_add_icon")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Icon",
                    tint = accentColor
                )
            }

            // Save / Done Button
            Button(
                onClick = {
                    settingsManager.saveShortsActionConfigs(localConfigs)
                    Toast.makeText(context, "Shorts icon layout saved!", Toast.LENGTH_SHORT).show()
                    onBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(start = 4.dp).testTag("demo_shorts_save")
            ) {
                Text("Done", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // Trash / Removal Area Indicator (Always visible when dragging, or at the top)
        val trashActive = draggedActionId != null
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (trashActive) 56.dp else 40.dp)
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isDraggingOverTrash) Color(0xEEB71C1C)
                    else if (trashActive) Color(0x77B71C1C)
                    else Color(0x22FFFFFF)
                )
                .border(
                    width = if (isDraggingOverTrash) 2.dp else 1.dp,
                    color = if (isDraggingOverTrash) Color.Red else Color.White.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isDraggingOverTrash) Icons.Filled.Delete else Icons.Default.DeleteOutline,
                    contentDescription = "Trash",
                    tint = if (isDraggingOverTrash) Color.White else Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isDraggingOverTrash) "Release here to remove icon!" else "Drag icon here to hide from Shorts",
                    color = Color.White,
                    fontWeight = if (isDraggingOverTrash) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.sp
                )
            }
        }

        // Interactive Demo Shorts Preview Viewport (Portrait 9:16 aspect ratio)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(9f / 16f)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1E293B),
                                Color(0xFF0F172A),
                                Color(0xFF020617)
                            )
                        )
                    )
                    .testTag("demo_shorts_viewport")
            ) {
                val viewportWidth = maxWidth
                val viewportHeight = maxHeight

                // Decorative video mockup lines
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0x3338BDF8), Color.Transparent),
                                radius = 400f
                            )
                        )
                )

                // Demo Info Section (Bottom Left)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth(0.68f)
                        .padding(start = 12.dp, bottom = 16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(YtAvatarTeal),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("M", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "@mytube_creator",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Demo Shorts Preview • Liquid Glass Optics #MyTube",
                        color = Color.White,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x55000000))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("🎵 Original audio - Glass Beat", color = Color.White, fontSize = 9.sp)
                    }
                }

                // Render Customizable Action Icons with Drag Gesture
                localConfigs.filter { it.isVisible }.forEach { action ->
                    var isDragModeUnlocked by remember(action.id) { mutableStateOf(false) }
                    var currentXRatio by remember(action.id, action.xRatio) { mutableFloatStateOf(action.xRatio) }
                    var currentYRatio by remember(action.id, action.yRatio) { mutableFloatStateOf(action.yRatio) }

                    val pixelX = (viewportWidth.value * currentXRatio).dp
                    val pixelY = (viewportHeight.value * currentYRatio).dp

                    Box(
                        modifier = Modifier
                            .offset(x = pixelX.coerceIn(4.dp, viewportWidth - 54.dp), y = pixelY.coerceIn(4.dp, viewportHeight - 54.dp))
                            .pointerInput(action.id) {
                                detectTapGestures(
                                    onLongPress = {
                                        isDragModeUnlocked = true
                                        draggedActionId = action.id
                                        Toast.makeText(context, "${action.name} drag mode active! Move icon now.", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                            .pointerInput(action.id) {
                                detectDragGestures(
                                    onDragStart = {
                                        draggedActionId = action.id
                                    },
                                    onDragEnd = {
                                        if (isDraggingOverTrash) {
                                            if (action.isProtected) {
                                                Toast.makeText(context, "${action.name} is a protected core control and cannot be removed.", Toast.LENGTH_SHORT).show()
                                            } else {
                                                // Remove/Hide icon
                                                localConfigs = localConfigs.map {
                                                    if (it.id == action.id) it.copy(isVisible = false) else it
                                                }
                                                settingsManager.saveShortsActionConfigs(localConfigs)
                                                Toast.makeText(context, "${action.name} removed from Shorts. Restore via '+' button.", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            // Commit new position
                                            localConfigs = localConfigs.map {
                                                if (it.id == action.id) it.copy(xRatio = currentXRatio, yRatio = currentYRatio) else it
                                            }
                                            settingsManager.saveShortsActionConfigs(localConfigs)
                                        }
                                        draggedActionId = null
                                        isDraggingOverTrash = false
                                        isDragModeUnlocked = false
                                    },
                                    onDragCancel = {
                                        draggedActionId = null
                                        isDraggingOverTrash = false
                                        isDragModeUnlocked = false
                                    }
                                ) { change, dragAmount ->
                                    change.consume()
                                    val deltaXRatio = dragAmount.x / (viewportWidth.value * density)
                                    val deltaYRatio = dragAmount.y / (viewportHeight.value * density)
                                    currentXRatio = (currentXRatio + deltaXRatio).coerceIn(0.04f, 0.92f)
                                    currentYRatio = (currentYRatio + deltaYRatio).coerceIn(0.04f, 0.95f)

                                    // If dragged near the top of the viewport (trash zone)
                                    isDraggingOverTrash = currentYRatio <= 0.12f
                                }
                            }
                            .testTag("demo_action_${action.id}")
                    ) {
                        val isBeingDragged = draggedActionId == action.id
                        DemoActionIconView(
                            action = action,
                            isDragging = isBeingDragged,
                            isUnlocked = isDragModeUnlocked,
                            accentColor = accentColor
                        )
                    }
                }
            }
        }
    }

    // Add Icon Selector Dialog (Restores hidden supported icons)
    if (showAddIconDialog) {
        val hiddenIcons = localConfigs.filter { !it.isVisible }
        AlertDialog(
            onDismissRequest = { showAddIconDialog = false },
            title = {
                Text("Add / Restore Shorts Icon", fontWeight = FontWeight.Bold, color = Color.White)
            },
            text = {
                if (hiddenIcons.isEmpty()) {
                    Text("All supported Shorts action icons are currently visible in the preview!", color = Color.White.copy(alpha = 0.8f))
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Select an icon to restore to your Shorts UI:", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                        hiddenIcons.forEach { hidden ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x22FFFFFF))
                                    .clickable {
                                        localConfigs = localConfigs.map {
                                            if (it.id == hidden.id) it.copy(isVisible = true) else it
                                        }
                                        settingsManager.saveShortsActionConfigs(localConfigs)
                                        showAddIconDialog = false
                                        Toast.makeText(context, "${hidden.name} restored!", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = hidden.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Text("Restore", color = accentColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddIconDialog = false }) {
                    Text("Close", color = accentColor)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}

@Composable
private fun DemoActionIconView(
    action: ShortsActionConfig,
    isDragging: Boolean,
    isUnlocked: Boolean,
    accentColor: Color
) {
    val glassConfig = LocalLiquidGlassConfig.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .then(
                if (isDragging) {
                    Modifier
                        .shadow(12.dp, CircleShape)
                        .border(2.dp, accentColor, CircleShape)
                        .background(Color(0xCC000000), CircleShape)
                } else if (isUnlocked) {
                    Modifier
                        .border(1.5.dp, Color.Yellow, CircleShape)
                        .background(Color(0x66000000), CircleShape)
                } else Modifier
            )
            .padding(4.dp)
    ) {
        val iconVector: ImageVector = when (action.id) {
            "like" -> Icons.Filled.ThumbUp
            "dislike" -> Icons.Outlined.ThumbDown
            "comment" -> Icons.Outlined.ChatBubbleOutline
            "share" -> Icons.Outlined.Share
            "remix" -> Icons.Default.Repeat
            "more" -> Icons.Default.MoreVert
            "audio" -> Icons.Default.MusicNote
            else -> Icons.Default.ThumbUp
        }

        if (action.id == "subscribe") {
            // Subscribe pill in demo
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("Subscribe", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }
        } else {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .liquidGlassSurface(
                        shape = CircleShape,
                        config = glassConfig,
                        accentColor = accentColor,
                        elevation = 4.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = action.name,
                    tint = if (action.id == "like") accentColor else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = action.name,
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}
