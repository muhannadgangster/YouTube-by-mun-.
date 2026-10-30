package com.example.launcher.model

import android.graphics.Bitmap

/**
 * Represents an installed launchable application on the Android device.
 */
data class LauncherApp(
    val id: String, // format: "packageName/activityName"
    val packageName: String,
    val activityName: String,
    val label: String,
    val customLabel: String? = null,
    val customIconUri: String? = null,
    val category: AppCategory = AppCategory.OTHERS,
    val isPinnedToDock: Boolean = false,
    val isHidden: Boolean = false,
    val installTime: Long = 0L
) {
    val displayLabel: String
        get() = if (!customLabel.isNullOrBlank()) customLabel else label
}

/**
 * iOS App Library categories.
 */
enum class AppCategory(val displayName: String) {
    SUGGESTIONS("Suggestions"),
    SOCIAL("Social"),
    UTILITIES("Utilities"),
    CREATIVITY("Creativity"),
    ENTERTAINMENT("Entertainment"),
    PRODUCTIVITY("Productivity"),
    GAMES("Games"),
    OTHERS("Other Apps")
}

/**
 * An iOS-style folder holding a list of app IDs.
 */
data class AppFolder(
    val id: String,
    val name: String,
    val appIds: List<String> = emptyList(),
    val pageIndex: Int = 0,
    val cellIndex: Int = 0
)

/**
 * A slot on a Home Screen page.
 */
sealed class HomeScreenSlot {
    data class App(val appId: String) : HomeScreenSlot()
    data class Folder(val folderId: String) : HomeScreenSlot()
    data class Widget(val widgetType: WidgetType, val spanCols: Int = 2, val spanRows: Int = 2) : HomeScreenSlot()
}

/**
 * Supported iOS-style launcher widgets.
 */
enum class WidgetType(val title: String, val subtitle: String) {
    CLOCK_BATTERY("Clock & Battery", "Depth clock with live battery status"),
    WEATHER("Weather & Astronomy", "Local offline weather conditions"),
    QUICK_ACTIONS("Quick Shortcuts", "Flashlight, Calculator, Notes, Camera"),
    SYSTEM_STATS("Storage & RAM", "Optimized health monitor for Android Go")
}

/**
 * Real-time Liquid Glass engine configuration.
 */
data class GlassSettings(
    val isEnabled: Boolean = true,
    val transparency: Float = 0.22f, // 0.0 to 1.0
    val blurRadiusDp: Float = 24f, // 0 to 50
    val refractionStrength: Float = 16f, // 0 to 60
    val edgeHighlightStrength: Float = 65f, // 0 to 100
    val reflectionSheen: Float = 45f, // 0 to 100
    val tintDarkness: Float = 28f, // 0 to 100
    val cornerRadiusDp: Float = 22f, // 0 to 40
    val performanceMode: String = "HIGH", // "HIGH", "MEDIUM", "LOW"
    val animSpeed: Float = 100f
)

/**
 * Home screen grid and icon customization.
 */
data class HomeScreenConfig(
    val gridColumns: Int = 4,
    val gridRows: Int = 5,
    val iconSizeScale: Float = 1.0f, // 0.8f to 1.25f
    val showLabels: Boolean = true,
    val iconShape: String = "SQUIRCLE", // "SQUIRCLE", "CIRCLE", "ROUNDED_SQUARE"
    val themeMode: String = "DARK", // "DARK", "LIGHT", "AUTO"
    val dockCount: Int = 4,
    val wallpaperId: String = "liquid_mesh", // "liquid_mesh", "midnight_blue", "obsidian", "sunset_prism", "emerald_glow", "custom"
    val customWallpaperUri: String? = null,
    val wallpaperBlurDp: Float = 0f,
    val wallpaperDim: Float = 0.12f,
    val clockStyle: String = "SF_BOLD", // "SF_BOLD", "SF_THIN", "SERIF", "ROUNDED"
    val hapticFeedback: Boolean = true,
    val reducedMotion: Boolean = false
)
