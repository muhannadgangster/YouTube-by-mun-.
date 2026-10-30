package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.launcher.data.AppManager
import com.example.launcher.data.LauncherPreferencesManager
import com.example.launcher.glass.LiquidGlassSurface
import com.example.launcher.glass.LocalLauncherGlassSettings
import com.example.launcher.model.AppFolder
import com.example.launcher.model.GlassSettings
import com.example.launcher.model.HomeScreenConfig
import com.example.launcher.model.LauncherApp
import com.example.launcher.ui.controlcenter.ControlCenterOverlay
import com.example.launcher.ui.home.HomeScreen
import com.example.launcher.ui.lockscreen.LockScreenOverlay
import com.example.launcher.ui.notifications.NotificationCenterOverlay
import com.example.launcher.ui.search.SpotlightSearchOverlay
import com.example.launcher.ui.settings.LauncherSettingsScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var appManager: AppManager
    private lateinit var preferencesManager: LauncherPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appManager = AppManager(context = applicationContext, scope = lifecycleScope)
        preferencesManager = LauncherPreferencesManager(context = applicationContext)

        setContent {
            val scope = rememberCoroutineScope()

            val rawInstalledApps by appManager.installedApps.collectAsStateWithLifecycle()
            val homeConfig by preferencesManager.homeConfig.collectAsStateWithLifecycle(initialValue = HomeScreenConfig())
            val glassSettings by preferencesManager.glassSettings.collectAsStateWithLifecycle(initialValue = GlassSettings())
            val customTitles by preferencesManager.customTitles.collectAsStateWithLifecycle(initialValue = emptyMap())
            val customIcons by preferencesManager.customIcons.collectAsStateWithLifecycle(initialValue = emptyMap())
            val hiddenApps by preferencesManager.hiddenApps.collectAsStateWithLifecycle(initialValue = emptySet())
            val dockAppIds by preferencesManager.dockApps.collectAsStateWithLifecycle(initialValue = emptyList())
            val pagesLayout by preferencesManager.pagesLayout.collectAsStateWithLifecycle(initialValue = emptyList())
            val folders by preferencesManager.folders.collectAsStateWithLifecycle(initialValue = emptyList())
            val searchHistory by preferencesManager.searchHistory.collectAsStateWithLifecycle(initialValue = emptyList())

            // Apply user-defined custom titles, custom icons, and filter out hidden apps
            val processedApps = remember(rawInstalledApps, customTitles, customIcons, hiddenApps) {
                rawInstalledApps
                    .filterNot { hiddenApps.contains(it.id) }
                    .map { app ->
                        val customT = customTitles[app.id]
                        val customI = customIcons[app.id]
                        app.copy(
                            customLabel = customT,
                            customIconUri = customI
                        )
                    }
            }

            // Overlay display states
            var isControlCenterOpen by remember { mutableStateOf(false) }
            var isLockScreenOpen by remember { mutableStateOf(false) }
            var isNotificationsOpen by remember { mutableStateOf(false) }
            var isSpotlightOpen by remember { mutableStateOf(false) }
            var isSettingsOpen by remember { mutableStateOf(false) }

            // Android Hardware / Gesture Back Button handling
            BackHandler(
                enabled = isControlCenterOpen || isLockScreenOpen || isNotificationsOpen || isSpotlightOpen || isSettingsOpen
            ) {
                when {
                    isControlCenterOpen -> isControlCenterOpen = false
                    isLockScreenOpen -> isLockScreenOpen = false
                    isNotificationsOpen -> isNotificationsOpen = false
                    isSpotlightOpen -> isSpotlightOpen = false
                    isSettingsOpen -> isSettingsOpen = false
                }
            }

            val isDarkTheme = when (homeConfig.themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            val colorScheme = if (isDarkTheme) {
                darkColorScheme(
                    primary = Color(0xFF3B82F6),
                    background = Color(0xFF0F0F12),
                    surface = Color(0xFF1C1C22)
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFF2563EB),
                    background = Color(0xFFF2F2F7),
                    surface = Color(0xFFFFFFFF)
                )
            }

            MaterialTheme(colorScheme = colorScheme) {
                CompositionLocalProvider(
                    LocalLauncherGlassSettings provides glassSettings
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (isSettingsOpen) {
                            LauncherSettingsScreen(
                                config = homeConfig,
                                glassSettings = glassSettings,
                                preferencesManager = preferencesManager,
                                onBack = { isSettingsOpen = false },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            HomeScreen(
                                allApps = processedApps,
                                appManager = appManager,
                                config = homeConfig,
                                preferencesManager = preferencesManager,
                                folders = folders,
                                pagesLayout = pagesLayout,
                                dockAppIds = dockAppIds,
                                onOpenControlCenter = { isControlCenterOpen = true },
                                onOpenLockScreen = { isLockScreenOpen = true },
                                onOpenNotifications = { isNotificationsOpen = true },
                                onOpenSpotlight = { isSpotlightOpen = true },
                                onOpenSettings = { isSettingsOpen = true },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // iOS 27 Control Center Overlay
                        ControlCenterOverlay(
                            isVisible = isControlCenterOpen,
                            onDismiss = { isControlCenterOpen = false },
                            isDarkMode = isDarkTheme,
                            onToggleDarkMode = {
                                val nextMode = if (isDarkTheme) "LIGHT" else "DARK"
                                scope.launch {
                                    preferencesManager.updateHomeConfig(homeConfig.copy(themeMode = nextMode))
                                }
                            }
                        )

                        // iOS 27 Lock Screen Experience Overlay
                        LockScreenOverlay(
                            isVisible = isLockScreenOpen,
                            config = homeConfig,
                            onUnlock = { isLockScreenOpen = false }
                        )

                        // iOS Notification Center Overlay
                        NotificationCenterOverlay(
                            isVisible = isNotificationsOpen,
                            onDismiss = { isNotificationsOpen = false }
                        )

                        // iOS Spotlight Search Overlay
                        SpotlightSearchOverlay(
                            isVisible = isSpotlightOpen,
                            allApps = processedApps,
                            recentSearches = searchHistory,
                            getIconBitmap = { appManager.getAppIconBitmap(it) },
                            onAppClick = { app ->
                                appManager.launchApp(app)
                                isSpotlightOpen = false
                            },
                            onClearHistory = {
                                scope.launch { preferencesManager.clearSearchHistory() }
                            },
                            onSearchSubmit = { term ->
                                scope.launch { preferencesManager.addSearchHistory(term) }
                            },
                            onDismiss = { isSpotlightOpen = false }
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        appManager.cleanup()
    }
}
