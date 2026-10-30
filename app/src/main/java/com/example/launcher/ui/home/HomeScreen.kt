package com.example.launcher.ui.home

import android.content.Context
import android.graphics.Bitmap
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.launcher.data.AppManager
import com.example.launcher.data.LauncherPreferencesManager
import com.example.launcher.glass.LiquidGlassSurface
import com.example.launcher.model.AppFolder
import com.example.launcher.model.HomeScreenConfig
import com.example.launcher.model.LauncherApp
import com.example.launcher.model.WidgetType
import com.example.launcher.ui.applibrary.AppLibraryView
import com.example.launcher.ui.widgets.LauncherWidgetCard
import com.example.launcher.wallpaper.WallpaperBackground
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    allApps: List<LauncherApp>,
    appManager: AppManager,
    config: HomeScreenConfig,
    preferencesManager: LauncherPreferencesManager,
    folders: List<AppFolder>,
    pagesLayout: List<List<String>>,
    dockAppIds: List<String>,
    onOpenControlCenter: () -> Unit,
    onOpenLockScreen: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenSpotlight: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }

    var isEditMode by remember { mutableStateOf(false) }
    var selectedAppForCustomization by remember { mutableStateOf<LauncherApp?>(null) }
    var expandedFolder by remember { mutableStateOf<AppFolder?>(null) }

    fun triggerHaptic() {
        if (config.hapticFeedback && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    // Default Dock Apps resolution (Phone, Messages, Browser, Camera or first 4 apps)
    val resolvedDockApps = remember(dockAppIds, allApps) {
        if (dockAppIds.isNotEmpty()) {
            dockAppIds.mapNotNull { id -> allApps.find { it.id == id } }
        } else {
            // Smart auto-selection of essential dock apps
            val phone = allApps.find { it.packageName.contains("dialer") || it.packageName.contains("phone") }
            val msg = allApps.find { it.packageName.contains("message") || it.packageName.contains("sms") }
            val browser = allApps.find { it.packageName.contains("chrome") || it.packageName.contains("browser") }
            val camera = allApps.find { it.packageName.contains("camera") }
            val selected = listOfNotNull(phone, msg, browser, camera)
            if (selected.size >= 4) selected
            else (selected + allApps.filterNot { selected.contains(it) }).take(4)
        }
    }

    // Generate home pages: Page 0 is Widgets, Page 1..N are Apps, Page N+1 is App Library
    val totalHomePages = remember(allApps, pagesLayout) {
        val appsPerPage = config.gridColumns * config.gridRows
        val numPages = if (pagesLayout.isNotEmpty()) {
            pagesLayout.size.coerceAtLeast(1)
        } else {
            val nonDockApps = allApps.filterNot { resolvedDockApps.contains(it) }
            val needed = (nonDockApps.size + appsPerPage - 1) / appsPerPage
            needed.coerceAtLeast(1)
        }
        numPages
    }

    // Pager: 0 = Today View / Widgets, 1..totalHomePages = Home grids, totalHomePages+1 = App Library
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { totalHomePages + 2 })

    Box(modifier = modifier.fillMaxSize()) {
        // High-Resolution Wallpaper Layer
        WallpaperBackground(config = config)

        // Gesture Detection Layer for Control Center, Lock Screen, Spotlight
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            val startX = change.position.x
                            val startY = change.position.y
                            val screenW = size.width

                            // Pull down from top edge gestures
                            if (startY < 200f && dragAmount.y > 25f) {
                                if (startX > screenW * 0.70f) {
                                    // Top right -> Control Center
                                    triggerHaptic()
                                    onOpenControlCenter()
                                } else if (startX < screenW * 0.30f) {
                                    // Top left -> Notification Center
                                    triggerHaptic()
                                    onOpenNotifications()
                                } else {
                                    // Top middle -> Spotlight Search
                                    triggerHaptic()
                                    onOpenSpotlight()
                                }
                            }
                        }
                    )
                }
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar with Edit Mode Done button & Settings
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isEditMode) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable {
                                // Add widget shortcut
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(0)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Widget", tint = Color.White)
                    }

                    Button(
                        onClick = { isEditMode = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Text("Done", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.size(36.dp))
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Swipeable Horizontal Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                when (pageIndex) {
                    0 -> {
                        // iOS Today View / Widgets Stack
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                LauncherWidgetCard(
                                    widgetType = WidgetType.CLOCK_BATTERY,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            item {
                                LauncherWidgetCard(
                                    widgetType = WidgetType.WEATHER,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            item {
                                LauncherWidgetCard(
                                    widgetType = WidgetType.QUICK_ACTIONS,
                                    onQuickActionClick = { action ->
                                        when (action) {
                                            "torch" -> onOpenControlCenter()
                                            "calculator" -> onOpenControlCenter()
                                            "search" -> onOpenSpotlight()
                                            "camera" -> {
                                                try {
                                                    context.startActivity(android.content.Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
                                                } catch (_: Exception) {}
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            item {
                                LauncherWidgetCard(
                                    widgetType = WidgetType.SYSTEM_STATS,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    in 1..totalHomePages -> {
                        // Home Screen Page Grid
                        val homePageIndex = pageIndex - 1
                        val appsPerPage = config.gridColumns * config.gridRows
                        val nonDockApps = allApps.filterNot { resolvedDockApps.contains(it) }
                        val pageApps = nonDockApps.drop(homePageIndex * appsPerPage).take(appsPerPage)

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .combinedClickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { if (isEditMode) isEditMode = false },
                                    onLongClick = {
                                        triggerHaptic()
                                        isEditMode = true
                                    }
                                )
                        ) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(config.gridColumns),
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(22.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                itemsIndexed(pageApps, key = { _, app -> app.id }) { index, app ->
                                    AppIconItem(
                                        app = app,
                                        iconBitmap = appManager.getAppIconBitmap(app),
                                        config = config,
                                        isEditMode = isEditMode,
                                        itemIndex = index,
                                        onClick = {
                                            if (isEditMode) {
                                                selectedAppForCustomization = app
                                            } else {
                                                appManager.launchApp(app)
                                            }
                                        },
                                        onLongClick = {
                                            triggerHaptic()
                                            isEditMode = true
                                            selectedAppForCustomization = app
                                        },
                                        onRemoveClick = {
                                            selectedAppForCustomization = app
                                        }
                                    )
                                }
                            }
                        }
                    }

                    else -> {
                        // iOS App Library Page
                        AppLibraryView(
                            allApps = allApps,
                            getIconBitmap = { appManager.getAppIconBitmap(it) },
                            config = config,
                            onAppClick = { appManager.launchApp(it) },
                            onAppLongClick = {
                                triggerHaptic()
                                selectedAppForCustomization = it
                            }
                        )
                    }
                }
            }

            // iOS Page Indicator Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 10.dp)
            ) {
                for (i in 0 until (totalHomePages + 2)) {
                    val isSelected = pagerState.currentPage == i
                    Box(
                        modifier = Modifier
                            .height(6.5.dp)
                            .width(if (isSelected) 18.dp else 6.5.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color.White else Color.White.copy(alpha = 0.35f)
                            )
                            .clickable {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(i)
                                }
                            }
                    )
                }
            }

            // Bottom iOS Liquid Glass Dock
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                LiquidGlassSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(86.dp),
                    shape = RoundedCornerShape(32.dp),
                    cornerRadius = 32.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        resolvedDockApps.forEachIndexed { index, app ->
                            AppIconItem(
                                app = app,
                                iconBitmap = appManager.getAppIconBitmap(app),
                                config = config.copy(showLabels = false),
                                isEditMode = isEditMode,
                                itemIndex = index,
                                onClick = {
                                    if (isEditMode) {
                                        selectedAppForCustomization = app
                                    } else {
                                        appManager.launchApp(app)
                                    }
                                },
                                onLongClick = {
                                    triggerHaptic()
                                    isEditMode = true
                                    selectedAppForCustomization = app
                                },
                                onRemoveClick = {
                                    selectedAppForCustomization = app
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Custom Icon & App Renaming Dialog
    if (selectedAppForCustomization != null) {
        val app = selectedAppForCustomization!!
        CustomIconEditDialog(
            app = app,
            currentBitmap = appManager.getAppIconBitmap(app),
            onSave = { newTitle, customBitmap, resetIcon ->
                coroutineScope.launch {
                    preferencesManager.setCustomAppTitle(app.id, newTitle)
                    if (resetIcon) {
                        appManager.resetCustomIcon(app.id)
                        preferencesManager.setCustomAppIcon(app.id, null)
                    } else if (customBitmap != null) {
                        val savedPath = appManager.saveCustomIcon(app.id, customBitmap)
                        preferencesManager.setCustomAppIcon(app.id, savedPath)
                    }
                    appManager.reloadApps()
                }
            },
            onHideApp = { hide ->
                coroutineScope.launch {
                    preferencesManager.setAppHidden(app.id, hide)
                    appManager.reloadApps()
                }
            },
            onAppInfoClick = {
                appManager.openAppDetails(app.packageName)
            },
            onUninstallClick = {
                appManager.uninstallApp(app.packageName)
            },
            onDismiss = { selectedAppForCustomization = null }
        )
    }

    // Folder Expanded Dialog
    if (expandedFolder != null) {
        val folder = expandedFolder!!
        val appsInFolder = folder.appIds.mapNotNull { id -> allApps.find { it.id == id } }
        FolderExpandedDialog(
            folder = folder,
            appsInFolder = appsInFolder,
            getIconBitmap = { appManager.getAppIconBitmap(it) },
            config = config,
            onAppClick = { appManager.launchApp(it) },
            onRenameFolder = { newName ->
                coroutineScope.launch {
                    preferencesManager.saveFolder(folder.copy(name = newName))
                }
            },
            onDismiss = { expandedFolder = null }
        )
    }
}
