package com.example.launcher.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.launcher.model.AppFolder
import com.example.launcher.model.GlassSettings
import com.example.launcher.model.HomeScreenConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

val Context.launcherDataStore: DataStore<Preferences> by preferencesDataStore(name = "ios27_launcher_prefs")

class LauncherPreferencesManager(private val context: Context) {

    companion object {
        // Glass settings
        val KEY_GLASS_ENABLED = booleanPreferencesKey("glass_enabled")
        val KEY_GLASS_TRANSPARENCY = floatPreferencesKey("glass_transparency")
        val KEY_GLASS_BLUR = floatPreferencesKey("glass_blur")
        val KEY_GLASS_REFRACTION = floatPreferencesKey("glass_refraction")
        val KEY_GLASS_EDGE_HIGHLIGHT = floatPreferencesKey("glass_edge_highlight")
        val KEY_GLASS_REFLECTION = floatPreferencesKey("glass_reflection")
        val KEY_GLASS_DARKNESS = floatPreferencesKey("glass_darkness")
        val KEY_GLASS_CORNER_RADIUS = floatPreferencesKey("glass_corner_radius")
        val KEY_GLASS_PERF_MODE = stringPreferencesKey("glass_perf_mode")
        val KEY_GLASS_ANIM_SPEED = floatPreferencesKey("glass_anim_speed")

        // Home screen config
        val KEY_GRID_COLUMNS = intPreferencesKey("grid_columns")
        val KEY_GRID_ROWS = intPreferencesKey("grid_rows")
        val KEY_ICON_SIZE_SCALE = floatPreferencesKey("icon_size_scale")
        val KEY_SHOW_LABELS = booleanPreferencesKey("show_labels")
        val KEY_ICON_SHAPE = stringPreferencesKey("icon_shape")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_DOCK_COUNT = intPreferencesKey("dock_count")
        val KEY_WALLPAPER_ID = stringPreferencesKey("wallpaper_id")
        val KEY_CUSTOM_WALLPAPER_URI = stringPreferencesKey("custom_wallpaper_uri")
        val KEY_WALLPAPER_BLUR = floatPreferencesKey("wallpaper_blur")
        val KEY_WALLPAPER_DIM = floatPreferencesKey("wallpaper_dim")
        val KEY_CLOCK_STYLE = stringPreferencesKey("clock_style")
        val KEY_HAPTIC = booleanPreferencesKey("haptic_feedback")
        val KEY_REDUCED_MOTION = booleanPreferencesKey("reduced_motion")

        // Customizations & Layout
        val KEY_CUSTOM_TITLES_JSON = stringPreferencesKey("custom_titles_json")
        val KEY_CUSTOM_ICONS_JSON = stringPreferencesKey("custom_icons_json")
        val KEY_HIDDEN_APPS = stringSetPreferencesKey("hidden_apps")
        val KEY_DOCK_APPS = stringPreferencesKey("dock_apps_json")
        val KEY_PAGES_LAYOUT_JSON = stringPreferencesKey("pages_layout_json")
        val KEY_FOLDERS_JSON = stringPreferencesKey("folders_json")
        val KEY_SEARCH_HISTORY_JSON = stringPreferencesKey("search_history_json")
    }

    val glassSettings: Flow<GlassSettings> = context.launcherDataStore.data.map { prefs ->
        GlassSettings(
            isEnabled = prefs[KEY_GLASS_ENABLED] ?: true,
            transparency = prefs[KEY_GLASS_TRANSPARENCY] ?: 0.22f,
            blurRadiusDp = prefs[KEY_GLASS_BLUR] ?: 24f,
            refractionStrength = prefs[KEY_GLASS_REFRACTION] ?: 16f,
            edgeHighlightStrength = prefs[KEY_GLASS_EDGE_HIGHLIGHT] ?: 65f,
            reflectionSheen = prefs[KEY_GLASS_REFLECTION] ?: 45f,
            tintDarkness = prefs[KEY_GLASS_DARKNESS] ?: 28f,
            cornerRadiusDp = prefs[KEY_GLASS_CORNER_RADIUS] ?: 22f,
            performanceMode = prefs[KEY_GLASS_PERF_MODE] ?: "HIGH",
            animSpeed = prefs[KEY_GLASS_ANIM_SPEED] ?: 100f
        )
    }

    val homeConfig: Flow<HomeScreenConfig> = context.launcherDataStore.data.map { prefs ->
        HomeScreenConfig(
            gridColumns = prefs[KEY_GRID_COLUMNS] ?: 4,
            gridRows = prefs[KEY_GRID_ROWS] ?: 5,
            iconSizeScale = prefs[KEY_ICON_SIZE_SCALE] ?: 1.0f,
            showLabels = prefs[KEY_SHOW_LABELS] ?: true,
            iconShape = prefs[KEY_ICON_SHAPE] ?: "SQUIRCLE",
            themeMode = prefs[KEY_THEME_MODE] ?: "DARK",
            dockCount = prefs[KEY_DOCK_COUNT] ?: 4,
            wallpaperId = prefs[KEY_WALLPAPER_ID] ?: "liquid_mesh",
            customWallpaperUri = prefs[KEY_CUSTOM_WALLPAPER_URI],
            wallpaperBlurDp = prefs[KEY_WALLPAPER_BLUR] ?: 0f,
            wallpaperDim = prefs[KEY_WALLPAPER_DIM] ?: 0.12f,
            clockStyle = prefs[KEY_CLOCK_STYLE] ?: "SF_BOLD",
            hapticFeedback = prefs[KEY_HAPTIC] ?: true,
            reducedMotion = prefs[KEY_REDUCED_MOTION] ?: false
        )
    }

    val customTitles: Flow<Map<String, String>> = context.launcherDataStore.data.map { prefs ->
        parseJsonMap(prefs[KEY_CUSTOM_TITLES_JSON])
    }

    val customIcons: Flow<Map<String, String>> = context.launcherDataStore.data.map { prefs ->
        parseJsonMap(prefs[KEY_CUSTOM_ICONS_JSON])
    }

    val hiddenApps: Flow<Set<String>> = context.launcherDataStore.data.map { prefs ->
        prefs[KEY_HIDDEN_APPS] ?: emptySet()
    }

    val dockApps: Flow<List<String>> = context.launcherDataStore.data.map { prefs ->
        parseJsonList(prefs[KEY_DOCK_APPS])
    }

    val pagesLayout: Flow<List<List<String>>> = context.launcherDataStore.data.map { prefs ->
        parseJsonPages(prefs[KEY_PAGES_LAYOUT_JSON])
    }

    val folders: Flow<List<AppFolder>> = context.launcherDataStore.data.map { prefs ->
        parseFolders(prefs[KEY_FOLDERS_JSON])
    }

    val searchHistory: Flow<List<String>> = context.launcherDataStore.data.map { prefs ->
        parseJsonList(prefs[KEY_SEARCH_HISTORY_JSON])
    }

    suspend fun updateGlassSettings(settings: GlassSettings) {
        context.launcherDataStore.edit { prefs ->
            prefs[KEY_GLASS_ENABLED] = settings.isEnabled
            prefs[KEY_GLASS_TRANSPARENCY] = settings.transparency
            prefs[KEY_GLASS_BLUR] = settings.blurRadiusDp
            prefs[KEY_GLASS_REFRACTION] = settings.refractionStrength
            prefs[KEY_GLASS_EDGE_HIGHLIGHT] = settings.edgeHighlightStrength
            prefs[KEY_GLASS_REFLECTION] = settings.reflectionSheen
            prefs[KEY_GLASS_DARKNESS] = settings.tintDarkness
            prefs[KEY_GLASS_CORNER_RADIUS] = settings.cornerRadiusDp
            prefs[KEY_GLASS_PERF_MODE] = settings.performanceMode
            prefs[KEY_GLASS_ANIM_SPEED] = settings.animSpeed
        }
    }

    suspend fun updateHomeConfig(config: HomeScreenConfig) {
        context.launcherDataStore.edit { prefs ->
            prefs[KEY_GRID_COLUMNS] = config.gridColumns
            prefs[KEY_GRID_ROWS] = config.gridRows
            prefs[KEY_ICON_SIZE_SCALE] = config.iconSizeScale
            prefs[KEY_SHOW_LABELS] = config.showLabels
            prefs[KEY_ICON_SHAPE] = config.iconShape
            prefs[KEY_THEME_MODE] = config.themeMode
            prefs[KEY_DOCK_COUNT] = config.dockCount
            prefs[KEY_WALLPAPER_ID] = config.wallpaperId
            if (config.customWallpaperUri != null) {
                prefs[KEY_CUSTOM_WALLPAPER_URI] = config.customWallpaperUri
            } else {
                prefs.remove(KEY_CUSTOM_WALLPAPER_URI)
            }
            prefs[KEY_WALLPAPER_BLUR] = config.wallpaperBlurDp
            prefs[KEY_WALLPAPER_DIM] = config.wallpaperDim
            prefs[KEY_CLOCK_STYLE] = config.clockStyle
            prefs[KEY_HAPTIC] = config.hapticFeedback
            prefs[KEY_REDUCED_MOTION] = config.reducedMotion
        }
    }

    suspend fun setCustomAppTitle(appId: String, newTitle: String?) {
        context.launcherDataStore.edit { prefs ->
            val current = parseJsonMap(prefs[KEY_CUSTOM_TITLES_JSON]).toMutableMap()
            if (newTitle.isNullOrBlank()) {
                current.remove(appId)
            } else {
                current[appId] = newTitle.trim()
            }
            prefs[KEY_CUSTOM_TITLES_JSON] = toJsonMap(current)
        }
    }

    suspend fun setCustomAppIcon(appId: String, iconPath: String?) {
        context.launcherDataStore.edit { prefs ->
            val current = parseJsonMap(prefs[KEY_CUSTOM_ICONS_JSON]).toMutableMap()
            if (iconPath.isNullOrBlank()) {
                current.remove(appId)
            } else {
                current[appId] = iconPath
            }
            prefs[KEY_CUSTOM_ICONS_JSON] = toJsonMap(current)
        }
    }

    suspend fun setAppHidden(appId: String, hidden: Boolean) {
        context.launcherDataStore.edit { prefs ->
            val current = (prefs[KEY_HIDDEN_APPS] ?: emptySet()).toMutableSet()
            if (hidden) current.add(appId) else current.remove(appId)
            prefs[KEY_HIDDEN_APPS] = current
        }
    }

    suspend fun setDockApps(apps: List<String>) {
        context.launcherDataStore.edit { prefs ->
            prefs[KEY_DOCK_APPS] = toJsonList(apps)
        }
    }

    suspend fun setPagesLayout(pages: List<List<String>>) {
        context.launcherDataStore.edit { prefs ->
            prefs[KEY_PAGES_LAYOUT_JSON] = toJsonPages(pages)
        }
    }

    suspend fun saveFolder(folder: AppFolder) {
        context.launcherDataStore.edit { prefs ->
            val current = parseFolders(prefs[KEY_FOLDERS_JSON]).toMutableList()
            val index = current.indexOfFirst { it.id == folder.id }
            if (index >= 0) {
                current[index] = folder
            } else {
                current.add(folder)
            }
            prefs[KEY_FOLDERS_JSON] = toFoldersJson(current)
        }
    }

    suspend fun deleteFolder(folderId: String) {
        context.launcherDataStore.edit { prefs ->
            val current = parseFolders(prefs[KEY_FOLDERS_JSON]).filterNot { it.id == folderId }
            prefs[KEY_FOLDERS_JSON] = toFoldersJson(current)
        }
    }

    suspend fun addSearchHistory(query: String) {
        if (query.isBlank()) return
        context.launcherDataStore.edit { prefs ->
            val current = parseJsonList(prefs[KEY_SEARCH_HISTORY_JSON]).toMutableList()
            current.remove(query)
            current.add(0, query)
            val trimmed = current.take(10)
            prefs[KEY_SEARCH_HISTORY_JSON] = toJsonList(trimmed)
        }
    }

    suspend fun clearSearchHistory() {
        context.launcherDataStore.edit { prefs ->
            prefs.remove(KEY_SEARCH_HISTORY_JSON)
        }
    }

    suspend fun resetToDefaults() {
        context.launcherDataStore.edit { prefs ->
            prefs.clear()
        }
    }

    // Helper JSON serializers & parsers
    private fun parseJsonMap(raw: String?): Map<String, String> {
        if (raw.isNullOrBlank()) return emptyMap()
        return try {
            val obj = JSONObject(raw)
            val map = mutableMapOf<String, String>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = obj.getString(key)
            }
            map
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun toJsonMap(map: Map<String, String>): String {
        val obj = JSONObject()
        for ((k, v) in map) {
            obj.put(k, v)
        }
        return obj.toString()
    }

    private fun parseJsonList(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                list.add(arr.getString(i))
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun toJsonList(list: List<String>): String {
        val arr = JSONArray()
        for (item in list) {
            arr.put(item)
        }
        return arr.toString()
    }

    private fun parseJsonPages(raw: String?): List<List<String>> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(raw)
            val pages = mutableListOf<List<String>>()
            for (i in 0 until arr.length()) {
                val pageArr = arr.getJSONArray(i)
                val items = mutableListOf<String>()
                for (j in 0 until pageArr.length()) {
                    items.add(pageArr.getString(j))
                }
                pages.add(items)
            }
            pages
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun toJsonPages(pages: List<List<String>>): String {
        val arr = JSONArray()
        for (page in pages) {
            val pageArr = JSONArray()
            for (item in page) {
                pageArr.put(item)
            }
            arr.put(pageArr)
        }
        return arr.toString()
    }

    private fun parseFolders(raw: String?): List<AppFolder> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(raw)
            val folders = mutableListOf<AppFolder>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val id = obj.getString("id")
                val name = obj.getString("name")
                val pageIndex = obj.optInt("pageIndex", 0)
                val cellIndex = obj.optInt("cellIndex", 0)
                val appIdsArr = obj.optJSONArray("appIds") ?: JSONArray()
                val appIds = mutableListOf<String>()
                for (j in 0 until appIdsArr.length()) {
                    appIds.add(appIdsArr.getString(j))
                }
                folders.add(AppFolder(id, name, appIds, pageIndex, cellIndex))
            }
            folders
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun toFoldersJson(folders: List<AppFolder>): String {
        val arr = JSONArray()
        for (folder in folders) {
            val obj = JSONObject().apply {
                put("id", folder.id)
                put("name", folder.name)
                put("pageIndex", folder.pageIndex)
                put("cellIndex", folder.cellIndex)
                val appIdsArr = JSONArray()
                for (appId in folder.appIds) {
                    appIdsArr.put(appId)
                }
                put("appIds", appIdsArr)
            }
            arr.put(obj)
        }
        return arr.toString()
    }
}
