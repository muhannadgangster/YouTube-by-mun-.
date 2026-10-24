package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.SettingsManager
import com.example.data.model.AppElementCatalog
import com.example.data.model.AppElementConfig
import com.example.ui.components.ButtonArtworkCropDialog
import com.example.ui.theme.LocalAppAccentColor
import com.example.ui.theme.LocalLiquidGlassConfig
import com.example.ui.theme.YtDarkBackground
import com.example.ui.theme.YtRed
import com.example.ui.theme.liquidGlassSurface
import java.io.File
import kotlin.math.roundToInt

@Composable
fun DemoAndCustomizationScreen(
    settingsManager: SettingsManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val accentColor = LocalAppAccentColor.current
    val glassConfig = LocalLiquidGlassConfig.current

    val elementConfigs by settingsManager.appElementConfigs.collectAsStateWithLifecycle()
    val customArtworks by settingsManager.customButtonArtworks.collectAsStateWithLifecycle()
    val shortsDoubleTapMode by settingsManager.shortsDoubleTapMode.collectAsStateWithLifecycle()

    var localConfigs by remember(elementConfigs) { mutableStateOf(elementConfigs) }

    // Screen tab selection
    val screenTabs = listOf("Home", "Shorts", "Player", "Profile", "Subscriptions", "Search", "Navigation")
    var selectedScreenTab by remember { mutableStateOf("Home") }

    // Selection & Dragging state
    var selectedElementId by remember { mutableStateOf<String?>(null) }
    var draggedElementId by remember { mutableStateOf<String?>(null) }
    var isDraggingOverTrash by remember { mutableStateOf(false) }

    // Dialog state
    var showCatalogDialog by remember { mutableStateOf(false) }
    var pendingCropUri by remember { mutableStateOf<Uri?>(null) }
    var cropTargetElementId by remember { mutableStateOf<String?>(null) }
    var cropTargetElementName by remember { mutableStateOf<String>("") }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null && cropTargetElementId != null) {
            pendingCropUri = uri
        }
    }

    val selectedElement = localConfigs.find { it.id == selectedElementId }

    BackHandler {
        onBack()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YtDarkBackground)
            .statusBarsPadding()
    ) {
        // Top Toolbar with Cancel, Reset Default, and Apply buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("customization_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Demo & Customization",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Interactive preview • 3s hold to drag • Gallery artwork",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }

            TextButton(
                onClick = onBack,
                modifier = Modifier.testTag("customization_cancel_button")
            ) {
                Text("Cancel", color = Color.LightGray, fontSize = 12.sp)
            }

            IconButton(
                onClick = {
                    settingsManager.resetAppElementConfigs()
                    localConfigs = AppElementCatalog.getAllDefaultElements()
                    Toast.makeText(context, "Restored original layout", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.testTag("customization_reset_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Default",
                    tint = Color.LightGray
                )
            }

            Button(
                onClick = {
                    settingsManager.saveAppElementConfigs(localConfigs)
                    // Also synchronize ShortsActionConfigs
                    val shortsConfigs = localConfigs.filter { it.screen == "Shorts" }.map {
                        com.example.data.model.ShortsActionConfig(
                            id = it.id,
                            name = it.name,
                            isVisible = it.isVisible,
                            xRatio = it.xRatio,
                            yRatio = it.yRatio,
                            isProtected = it.isProtected,
                            scale = it.scale
                        )
                    }
                    settingsManager.saveShortsActionConfigs(shortsConfigs)
                    Toast.makeText(context, "Customizations applied to app!", Toast.LENGTH_SHORT).show()
                    onBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(start = 2.dp)
                    .testTag("customization_apply_button")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Apply", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // Screen Selector Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            screenTabs.forEach { tabName ->
                val isSelected = selectedScreenTab == tabName
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedScreenTab = tabName
                        selectedElementId = null
                    },
                    label = { Text(tabName, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = accentColor,
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF1E212D),
                        labelColor = Color.LightGray
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) accentColor else Color(0xFF33384A)
                    ),
                    modifier = Modifier.testTag("tab_$tabName")
                )
            }
        }

        // Removal / Trash Zone
        val isTrashActive = draggedElementId != null
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isTrashActive) 48.dp else 36.dp)
                .padding(horizontal = 14.dp, vertical = 2.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (isDraggingOverTrash) Color(0xFFC62828)
                    else if (isTrashActive) Color(0x66B71C1C)
                    else Color(0x1AFFFFFF)
                )
                .border(
                    width = if (isDraggingOverTrash) 2.dp else 1.dp,
                    color = if (isDraggingOverTrash) Color.Red else Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp)
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
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isDraggingOverTrash) "Release here to remove from $selectedScreenTab!"
                    else if (isTrashActive) "Drag into trash area to hide"
                    else "Hold icon 3s to drag • Drop here to remove",
                    color = Color.White,
                    fontWeight = if (isDraggingOverTrash) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 11.sp
                )
            }
        }

        // Live Interactive App Preview Viewport
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(9f / 16f)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
                    .background(Color(0xFF0F1117))
                    .testTag("preview_viewport")
            ) {
                val viewportWidth = maxWidth
                val viewportHeight = maxHeight

                // Render background mockup specific to active tab
                ScreenMockupBackground(selectedScreenTab)

                // Render customizable elements for the selected tab
                val currentScreenElements = localConfigs.filter { it.screen == selectedScreenTab && it.isVisible }

                currentScreenElements.forEach { config ->
                    val isDragged = draggedElementId == config.id
                    val isSelected = selectedElementId == config.id
                    val elementArtwork = customArtworks[config.id] ?: config.customArtworkPath

                    val xOffset = (config.xRatio * viewportWidth.value).dp
                    val yOffset = (config.yRatio * viewportHeight.value).dp

                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    xOffset.roundToPx() - (26.dp * config.scale).roundToPx(),
                                    yOffset.roundToPx() - (26.dp * config.scale).roundToPx()
                                )
                            }
                            .pointerInput(config.id) {
                                detectTapGestures(
                                    onTap = {
                                        selectedElementId = config.id
                                    },
                                    onLongPress = {
                                        draggedElementId = config.id
                                        selectedElementId = config.id
                                        Toast.makeText(context, "${config.name} unlocked for dragging", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                            .then(
                                if (isDragged) {
                                    Modifier.pointerInput(config.id) {
                                        detectDragGestures(
                                            onDragEnd = {
                                                if (isDraggingOverTrash) {
                                                    if (config.isProtected) {
                                                        Toast.makeText(context, "Protected control cannot be removed", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        localConfigs = localConfigs.map {
                                                            if (it.id == config.id) it.copy(isVisible = false) else it
                                                        }
                                                        Toast.makeText(context, "${config.name} removed. Restore with (+)", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                                draggedElementId = null
                                                isDraggingOverTrash = false
                                            },
                                            onDragCancel = {
                                                draggedElementId = null
                                                isDraggingOverTrash = false
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                val newX = (config.xRatio + dragAmount.x / (viewportWidth.toPx())).coerceIn(0.04f, 0.94f)
                                                val newY = (config.yRatio + dragAmount.y / (viewportHeight.toPx())).coerceIn(0.02f, 0.96f)

                                                isDraggingOverTrash = newY < 0.10f

                                                localConfigs = localConfigs.map {
                                                    if (it.id == config.id) it.copy(xRatio = newX, yRatio = newY) else it
                                                }
                                            }
                                        )
                                    }
                                } else Modifier
                            )
                    ) {
                        RenderCustomizableElement(
                            config = config,
                            isSelected = isSelected,
                            isDragged = isDragged,
                            customArtwork = elementArtwork,
                            accentColor = accentColor
                        )
                    }
                }
            }
        }

        // Bottom Selected Element Bar / Controls
        if (selectedElement != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                color = Color(0xFF1B1E29),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C3246))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedElement.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Size: ${String.format("%.1fx", selectedElement.scale)} • Screen: ${selectedElement.screen}",
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }

                        // Choose Photo Button
                        Button(
                            onClick = {
                                cropTargetElementId = selectedElement.id
                                cropTargetElementName = selectedElement.name
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("btn_choose_photo")
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Choose Photo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        if (customArtworks[selectedElement.id] != null || selectedElement.customArtworkPath != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            OutlinedButton(
                                onClick = {
                                    settingsManager.removeCustomButtonArtwork(selectedElement.id)
                                    localConfigs = localConfigs.map {
                                        if (it.id == selectedElement.id) it.copy(customArtworkPath = null) else it
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Reset Art", fontSize = 11.sp, color = Color.White)
                            }
                        }

                        IconButton(
                            onClick = { selectedElementId = null },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                        }
                    }

                    // Scale Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Scale", color = Color.LightGray, fontSize = 11.sp, modifier = Modifier.width(36.dp))
                        Slider(
                            value = selectedElement.scale,
                            onValueChange = { newScale ->
                                localConfigs = localConfigs.map {
                                    if (it.id == selectedElement.id) it.copy(scale = newScale) else it
                                }
                            },
                            valueRange = 0.6f..1.8f,
                            colors = SliderDefaults.colors(
                                thumbColor = accentColor,
                                activeTrackColor = accentColor
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(28.dp)
                        )
                    }
                }
            }
        }

        // Bottom Controls Bar (+ Catalog and Shorts Double Tap Mode)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Shorts Double Tap Mode Quick Switch
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xFF1B1E29), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Shorts Double-Tap: ",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
                Text(
                    text = shortsDoubleTapMode,
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clickable {
                            val nextMode = if (shortsDoubleTapMode == "Heart") "Thumb" else "Heart"
                            settingsManager.setShortsDoubleTapMode(nextMode)
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // Floating (+) Button to open Icon Catalog
            Button(
                onClick = { showCatalogDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262C3D)),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3E4760)),
                modifier = Modifier.testTag("open_icon_catalog_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Icon Catalog (+)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Photo Crop Dialog
    if (pendingCropUri != null && cropTargetElementId != null) {
        ButtonArtworkCropDialog(
            imageUri = pendingCropUri!!,
            elementId = cropTargetElementId!!,
            elementName = cropTargetElementName,
            onDismiss = {
                pendingCropUri = null
            },
            onArtworkSaved = { savedPath ->
                val targetId = cropTargetElementId!!
                settingsManager.setCustomButtonArtwork(targetId, savedPath)
                localConfigs = localConfigs.map {
                    if (it.id == targetId) it.copy(customArtworkPath = savedPath) else it
                }
                pendingCropUri = null
                Toast.makeText(context, "Artwork applied to $cropTargetElementName!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Icon Catalog Dialog
    if (showCatalogDialog) {
        IconCatalogDialog(
            currentScreen = selectedScreenTab,
            allConfigs = localConfigs,
            customArtworks = customArtworks,
            shortsDoubleTapMode = shortsDoubleTapMode,
            accentColor = accentColor,
            onDismiss = { showCatalogDialog = false },
            onRestoreElement = { id ->
                localConfigs = localConfigs.map {
                    if (it.id == id) it.copy(isVisible = true) else it
                }
                Toast.makeText(context, "Element restored to $selectedScreenTab!", Toast.LENGTH_SHORT).show()
            },
            onCustomizeArtwork = { id, name ->
                cropTargetElementId = id
                cropTargetElementName = name
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onSetShortsDoubleTapMode = { mode ->
                settingsManager.setShortsDoubleTapMode(mode)
            }
        )
    }
}

@Composable
private fun RenderCustomizableElement(
    config: AppElementConfig,
    isSelected: Boolean,
    isDragged: Boolean,
    customArtwork: String?,
    accentColor: Color
) {
    val sizeDp = (48.dp * config.scale)
    val isPill = config.id.contains("subscribe") || config.id.contains("edit") || config.id.contains("downloads")

    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(if (isPill) sizeDp * 2.2f else sizeDp, sizeDp)
                .clip(if (isPill) RoundedCornerShape(24.dp) else CircleShape)
                .background(
                    when {
                        isDragged -> Color(0xFFC62828).copy(alpha = 0.85f)
                        isSelected -> accentColor.copy(alpha = 0.35f)
                        else -> Color(0x33000000)
                    }
                )
                .border(
                    width = if (isDragged || isSelected) 2.5.dp else 1.dp,
                    color = when {
                        isDragged -> Color.Red
                        isSelected -> accentColor
                        else -> Color.White.copy(alpha = 0.4f)
                    },
                    shape = if (isPill) RoundedCornerShape(24.dp) else CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!customArtwork.isNullOrBlank()) {
                val file = File(customArtwork)
                AsyncImage(
                    model = if (file.exists()) file else customArtwork,
                    contentDescription = config.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(if (isPill) RoundedCornerShape(24.dp) else CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Render default vector icon for element
                val iconVector = getIconForElement(config.id)
                if (isPill) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Icon(iconVector, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (config.id.contains("subscribe")) "Subscribe" else config.name.take(9),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                } else {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = config.name,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp * config.scale)
                    )
                }
            }
        }

        // Selection Tag / Element Name chip
        if (isSelected || isDragged) {
            Surface(
                color = if (isDragged) Color.Red else accentColor,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-18).dp)
            ) {
                Text(
                    text = config.name,
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.5.dp),
                    maxLines = 1
                )
            }
        }
    }
}

private fun getIconForElement(id: String): ImageVector = when (id) {
    AppElementCatalog.SHORTS_LIKE, AppElementCatalog.PLAYER_LIKE -> Icons.Default.ThumbUp
    AppElementCatalog.SHORTS_DISLIKE -> Icons.Default.ThumbDown
    AppElementCatalog.SHORTS_COMMENT -> Icons.Outlined.ChatBubbleOutline
    AppElementCatalog.SHORTS_SHARE, AppElementCatalog.PLAYER_SHARE -> Icons.Outlined.Share
    AppElementCatalog.SHORTS_SOUND -> Icons.Default.MusicNote
    AppElementCatalog.SHORTS_MORE, AppElementCatalog.PLAYER_MORE -> Icons.Default.MoreVert
    AppElementCatalog.HOME_SEARCH -> Icons.Default.Search
    AppElementCatalog.HOME_NOTIFICATIONS -> Icons.Default.Notifications
    AppElementCatalog.HOME_PROFILE, AppElementCatalog.PROFILE_AVATAR -> Icons.Default.Person
    AppElementCatalog.PLAYER_SKIP_BACK -> Icons.Default.Replay10
    AppElementCatalog.PLAYER_SKIP_FWD -> Icons.Default.PlayArrow
    AppElementCatalog.NAV_HOME -> Icons.Default.Home
    AppElementCatalog.NAV_SHORTS -> Icons.Default.Whatshot
    AppElementCatalog.NAV_PLUS -> Icons.Default.Add
    AppElementCatalog.NAV_SUBSCRIPTIONS -> Icons.Default.Subtitles
    AppElementCatalog.NAV_YOU -> Icons.Default.Person
    "search_mic", "search_fab_mic" -> Icons.Default.Mic
    else -> Icons.Default.Favorite
}

@Composable
private fun ScreenMockupBackground(tab: String) {
    when (tab) {
        "Home" -> MockupHome()
        "Shorts" -> MockupShorts()
        "Player" -> MockupPlayer()
        "Profile" -> MockupProfile()
        "Subscriptions" -> MockupSubscriptions()
        "Search" -> MockupSearch()
        else -> MockupHome()
    }
}

@Composable
private fun MockupHome() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F14))
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(YtRed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text("MyTube", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF26A69A)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("U", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("All", "Shorts", "Offline", "Podcasts").forEachIndexed { idx, label ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (idx == 0) Color.White else Color(0xFF272727))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = label,
                        color = if (idx == 0) Color.Black else Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Feed Item 1 (Hero Video Card)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF334155))
                        )
                    ),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text("4K HDR", color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.8f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text("14:28", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00ACC1)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("M", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "MyTube Offline Showcase - Liquid Glass 3D",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Android Studio • 1.2M views • 2 hours ago",
                        color = Color.Gray,
                        fontSize = 9.sp
                    )
                }
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Feed Item 2
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF31102F), Color(0xFF1E0A24), Color(0xFF4A154B))
                        )
                    ),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.8f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text("08:45", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom Navigation Bar Dock
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0A0A0E))
                .border(width = 0.5.dp, color = Color.White.copy(alpha = 0.1f))
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("Home" to Icons.Default.Home, "Shorts" to Icons.Default.Whatshot, "Sub" to Icons.Default.Subtitles, "You" to Icons.Default.Person).forEach { (name, icon) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(icon, contentDescription = null, tint = if (name == "Home") Color.White else Color.Gray, modifier = Modifier.size(16.dp))
                    Text(name, color = if (name == "Home") Color.White else Color.Gray, fontSize = 8.sp)
                }
            }
        }
    }
}

@Composable
private fun MockupShorts() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF020617))
                )
            )
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .align(Alignment.TopCenter),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }

        // Center Play Watermark / Video indicator
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f))
                .align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(24.dp))
        }

        // Bottom-Left Info Column
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.72f)
                .padding(start = 10.dp, bottom = 12.dp)
        ) {
            // Channel row: Avatar + handle + single Subscribe button
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00897B)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("S", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text("@mytube_creator", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("Subscribe", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Realistic 3D Liquid Glass with optical refraction #Shorts #Customizer",
                color = Color.White,
                fontSize = 10.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Original sound - MyTube Beat", color = Color.White.copy(alpha = 0.8f), fontSize = 8.sp)
            }
        }
    }
}

@Composable
private fun MockupPlayer() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0F))
    ) {
        // 16:9 Video Player Viewport
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFF1E1B4B), Color(0xFF0F172A)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("04:30", color = Color.White, fontSize = 8.sp)
                    Text("14:10", color = Color.LightGray, fontSize = 8.sp)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .background(Color.Gray.copy(alpha = 0.4f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.32f)
                            .height(2.5.dp)
                            .background(YtRed)
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = "Building 3D Optical Liquid Glass in Jetpack Compose",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "780K views • 2 days ago • #Android #Kotlin",
                color = Color.Gray,
                fontSize = 9.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Channel row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF673AB7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("A", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("Android Masters", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("920K subscribers", color = Color.Gray, fontSize = 8.sp)
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Subscribe", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Like (42K)", "Share", "Download").forEach { label ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF222634))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(label, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Comments preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E212D))
                    .padding(8.dp)
            ) {
                Column {
                    Text("Comments • 842", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(3.dp))
                    Text("@dev_guru: The optical background refraction is incredible!", color = Color.LightGray, fontSize = 9.sp)
                }
            }
        }
    }
}

@Composable
private fun MockupProfile() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F16))
    ) {
        // Banner Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp)
                .background(
                    Brush.linearGradient(listOf(Color(0xFF312E81), Color(0xFF4C1D95), Color(0xFF1E1B4B)))
                )
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.offset(y = (-18).dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF009688))
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("U", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.offset(y = 8.dp)) {
                    Text("Offline Explorer", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("@mytube_user • 24 saved items", color = Color.Gray, fontSize = 9.sp)
                }
            }

            // Quick Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("24 Downloads", "12 Playlists", "148 History").forEach { stat ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1F2433))
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Text(stat, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text("Downloads & Storage", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1A1C24))
                    .padding(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF43A047)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("3.2 GB Free • Ready for Offline Play", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        Text("All videos stored locally on device", color = Color.Gray, fontSize = 8.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun MockupSubscriptions() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E0E14))
    ) {
        // Channel Stories/Avatars Horizontal Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("Android" to Color(0xFF3DDC84), "Kotlin" to Color(0xFF7F52FF), "Compose" to Color(0xFF4285F4), "Design" to Color(0xFFEA4335)).forEach { (name, col) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(col)
                            .border(1.5.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(name.take(1), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(name, color = Color.White, fontSize = 8.sp)
                }
            }
        }

        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("All", "Today", "Videos", "Shorts").forEachIndexed { idx, label ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (idx == 0) Color.White else Color(0xFF222634))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(label, color = if (idx == 0) Color.Black else Color.White, fontSize = 8.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Subscription Feed item
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF1E3A8A), Color(0xFF172554)))
                    ),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.8f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text("18:40", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Advanced Liquid Glass Shaders in Android",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text("Compose Masterclass • 4 hours ago", color = Color.Gray, fontSize = 8.sp)
        }
    }
}

@Composable
private fun MockupSearch() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1016))
    ) {
        // Search Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF202433))
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Liquid Glass 3D", color = Color.White, fontSize = 10.sp)
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(modifier = Modifier.width(6.dp))
            Icon(Icons.Default.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        }

        // Suggestions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            listOf("liquid glass refraction", "shorts action customizer", "photo to video slideshow", "offline 4k player").forEach { query ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(query, color = Color.White.copy(alpha = 0.85f), fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun IconCatalogDialog(
    currentScreen: String,
    allConfigs: List<AppElementConfig>,
    customArtworks: Map<String, String>,
    shortsDoubleTapMode: String,
    accentColor: Color,
    onDismiss: () -> Unit,
    onRestoreElement: (id: String) -> Unit,
    onCustomizeArtwork: (id: String, name: String) -> Unit,
    onSetShortsDoubleTapMode: (mode: String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Icon & Button Catalog",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 18.sp
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Section: Double-Tap Like Artwork (Heart vs Thumb)
                item {
                    Surface(
                        color = Color(0xFF222634),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Shorts Double-Tap Like Animation",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = shortsDoubleTapMode == "Heart",
                                    onClick = { onSetShortsDoubleTapMode("Heart") },
                                    label = { Text("Heart Mode") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = accentColor,
                                        selectedLabelColor = Color.White
                                    )
                                )
                                FilterChip(
                                    selected = shortsDoubleTapMode == "Thumb",
                                    onClick = { onSetShortsDoubleTapMode("Thumb") },
                                    label = { Text("Thumb Mode") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = accentColor,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TextButton(
                                    onClick = { onCustomizeArtwork(AppElementCatalog.ART_HEART, "Heart Animation") }
                                ) {
                                    Text("Photo for Heart", fontSize = 11.sp, color = accentColor)
                                }
                                TextButton(
                                    onClick = { onCustomizeArtwork(AppElementCatalog.ART_THUMB, "Thumb Animation") }
                                ) {
                                    Text("Photo for Thumb", fontSize = 11.sp, color = accentColor)
                                }
                            }
                        }
                    }
                }

                // Section: Screen elements
                items(allConfigs) { config ->
                    val customArt = customArtworks[config.id] ?: config.customArtworkPath
                    Surface(
                        color = Color(0xFF1B1E29),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2C3246)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!customArt.isNullOrBlank()) {
                                    AsyncImage(
                                        model = File(customArt),
                                        contentDescription = config.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        getIconForElement(config.id),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = config.name,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${config.screen} • ${if (config.isVisible) "Visible" else "Hidden"}",
                                    color = if (config.isVisible) Color.LightGray else Color(0xFFFF5252),
                                    fontSize = 11.sp
                                )
                            }

                            if (!config.isVisible) {
                                TextButton(
                                    onClick = { onRestoreElement(config.id) }
                                ) {
                                    Text("Restore", color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            IconButton(
                                onClick = { onCustomizeArtwork(config.id, config.name) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Image,
                                    contentDescription = "Customize Artwork",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color(0xFF161820)
    )
}
