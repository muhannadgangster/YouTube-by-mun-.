package com.example.launcher.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.LruCache
import com.example.launcher.model.AppCategory
import com.example.launcher.model.LauncherApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class AppManager(private val context: Context, private val scope: CoroutineScope) {

    private val packageManager: PackageManager = context.packageManager

    private val _installedApps = MutableStateFlow<List<LauncherApp>>(emptyList())
    val installedApps: StateFlow<List<LauncherApp>> = _installedApps.asStateFlow()

    // 16MB Icon LRU Cache for buttery smooth scrolling and minimal memory on Android Go
    private val iconCache = object : LruCache<String, Bitmap>(16 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount
        }
    }

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            reloadApps()
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        context.registerReceiver(packageReceiver, filter)
        reloadApps()
    }

    fun cleanup() {
        try {
            context.unregisterReceiver(packageReceiver)
        } catch (_: Exception) {}
        iconCache.evictAll()
    }

    fun reloadApps() {
        scope.launch(Dispatchers.IO) {
            val apps = queryInstalledLaunchableApps()
            _installedApps.value = apps
        }
    }

    private fun queryInstalledLaunchableApps(): List<LauncherApp> {
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                mainIntent,
                PackageManager.ResolveInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(mainIntent, 0)
        }

        val selfPackage = context.packageName
        val list = mutableListOf<LauncherApp>()

        for (info in resolveInfos) {
            val pkg = info.activityInfo.packageName
            val act = info.activityInfo.name
            // Don't show the launcher itself as a redundant icon inside the launcher,
            // unless wanted; we provide a dedicated iOS Settings app icon.
            if (pkg == selfPackage) continue

            val label = try {
                info.loadLabel(packageManager).toString()
            } catch (_: Exception) {
                pkg
            }

            val id = "$pkg/$act"
            val category = detectAppCategory(info)

            val installTime = try {
                val pkgInfo = packageManager.getPackageInfo(pkg, 0)
                pkgInfo.firstInstallTime
            } catch (_: Exception) {
                0L
            }

            list.add(
                LauncherApp(
                    id = id,
                    packageName = pkg,
                    activityName = act,
                    label = label,
                    category = category,
                    installTime = installTime
                )
            )
        }

        return list.sortedBy { it.label.lowercase() }
    }

    private fun detectAppCategory(info: ResolveInfo): AppCategory {
        val pkg = info.activityInfo.packageName.lowercase()
        val appInfo = info.activityInfo.applicationInfo

        // 1. API 26+ App Category flags
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            when (appInfo.category) {
                ApplicationInfo.CATEGORY_GAME -> return AppCategory.GAMES
                ApplicationInfo.CATEGORY_AUDIO,
                ApplicationInfo.CATEGORY_VIDEO -> return AppCategory.ENTERTAINMENT
                ApplicationInfo.CATEGORY_IMAGE -> return AppCategory.CREATIVITY
                ApplicationInfo.CATEGORY_SOCIAL -> return AppCategory.SOCIAL
                ApplicationInfo.CATEGORY_PRODUCTIVITY -> return AppCategory.PRODUCTIVITY
            }
        }

        // 2. Keyword heuristics
        return when {
            pkg.contains("camera") || pkg.contains("photo") || pkg.contains("gallery") ||
                    pkg.contains("image") || pkg.contains("canvas") || pkg.contains("draw") || pkg.contains("editor") -> {
                AppCategory.CREATIVITY
            }
            pkg.contains("message") || pkg.contains("sms") || pkg.contains("chat") ||
                    pkg.contains("phone") || pkg.contains("dialer") || pkg.contains("contact") ||
                    pkg.contains("whatsapp") || pkg.contains("telegram") || pkg.contains("signal") ||
                    pkg.contains("social") || pkg.contains("facebook") || pkg.contains("twitter") ||
                    pkg.contains("instagram") -> {
                AppCategory.SOCIAL
            }
            pkg.contains("calc") || pkg.contains("clock") || pkg.contains("setting") ||
                    pkg.contains("file") || pkg.contains("download") || pkg.contains("manager") ||
                    pkg.contains("tool") || pkg.contains("torch") || pkg.contains("compass") -> {
                AppCategory.UTILITIES
            }
            pkg.contains("youtube") || pkg.contains("music") || pkg.contains("player") ||
                    pkg.contains("video") || pkg.contains("radio") || pkg.contains("stream") ||
                    pkg.contains("spotify") || pkg.contains("netflix") -> {
                AppCategory.ENTERTAINMENT
            }
            pkg.contains("mail") || pkg.contains("note") || pkg.contains("calendar") ||
                    pkg.contains("doc") || pkg.contains("sheet") || pkg.contains("office") ||
                    pkg.contains("task") || pkg.contains("keep") || pkg.contains("drive") -> {
                AppCategory.PRODUCTIVITY
            }
            pkg.contains("game") || pkg.contains("play") || pkg.contains("arcade") -> {
                AppCategory.GAMES
            }
            else -> AppCategory.OTHERS
        }
    }

    /**
     * Efficiently loads and caches an application's icon as a Bitmap.
     */
    fun getAppIconBitmap(app: LauncherApp): Bitmap? {
        // 1. Check custom icon if saved by user
        if (!app.customIconUri.isNullOrBlank()) {
            val cachedCustom = iconCache.get(app.customIconUri)
            if (cachedCustom != null) return cachedCustom

            val file = File(app.customIconUri)
            if (file.exists()) {
                val decoded = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
                if (decoded != null) {
                    iconCache.put(app.customIconUri, decoded)
                    return decoded
                }
            }
        }

        // 2. Check cached system icon
        val cached = iconCache.get(app.id)
        if (cached != null) return cached

        // 3. Load from PackageManager
        val drawable = try {
            val intent = Intent().setClassName(app.packageName, app.activityName)
            val resolveInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.resolveActivity(intent, PackageManager.ResolveInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.resolveActivity(intent, 0)
            }
            resolveInfo?.loadIcon(packageManager) ?: packageManager.getApplicationIcon(app.packageName)
        } catch (_: Exception) {
            try {
                packageManager.getApplicationIcon(app.packageName)
            } catch (_: Exception) {
                null
            }
        } ?: return null

        val bitmap = drawableToBitmap(drawable)
        iconCache.put(app.id, bitmap)
        return bitmap
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 144
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 144
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    /**
     * Saves a user-selected gallery image as a customized offline icon.
     */
    suspend fun saveCustomIcon(appId: String, sourceBitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, "custom_icons")
        if (!dir.exists()) dir.mkdirs()

        val safeName = appId.replace('/', '_').replace(':', '_') + ".png"
        val targetFile = File(dir, safeName)

        // Resize to standard iOS 192x192 square icon
        val scaled = Bitmap.createScaledBitmap(sourceBitmap, 192, 192, true)
        FileOutputStream(targetFile).use { out ->
            scaled.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        // Update cache
        iconCache.put(targetFile.absolutePath, scaled)
        targetFile.absolutePath
    }

    /**
     * Deletes user's customized icon and reverts to default system app icon.
     */
    suspend fun resetCustomIcon(appId: String) = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, "custom_icons")
        val safeName = appId.replace('/', '_').replace(':', '_') + ".png"
        val file = File(dir, safeName)
        if (file.exists()) {
            file.delete()
        }
        iconCache.remove(file.absolutePath)
        iconCache.remove(appId)
    }

    /**
     * Safely launches an application with error handling.
     */
    fun launchApp(app: LauncherApp, onError: (String) -> Unit = {}): Boolean {
        return try {
            val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                val explicitIntent = Intent(Intent.ACTION_MAIN).apply {
                    setClassName(app.packageName, app.activityName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(explicitIntent)
                true
            }
        } catch (e: Exception) {
            onError("Unable to launch ${app.displayLabel}: ${e.localizedMessage ?: "App not found"}")
            false
        }
    }

    /**
     * Opens system app details / permissions settings.
     */
    fun openAppDetails(packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    /**
     * Triggers official Android package uninstallation prompt.
     */
    fun uninstallApp(packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }
}
