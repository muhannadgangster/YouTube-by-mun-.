package com.example.launcher.wallpaper

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.launcher.model.HomeScreenConfig

data class BundledWallpaper(
    val id: String,
    val name: String,
    val previewColor: Color,
    val backgroundBrush: Brush
)

object WallpaperProvider {

    val bundledWallpapers = listOf(
        BundledWallpaper(
            id = "liquid_mesh",
            name = "iOS 27 Liquid Mesh",
            previewColor = Color(0xFF6B4EE6),
            backgroundBrush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF8B5CF6), // Purple
                    Color(0xFF3B82F6), // Blue
                    Color(0xFF06B6D4), // Cyan
                    Color(0xFF0F172A)  // Deep slate
                ),
                center = Offset(300f, 400f),
                radius = 1200f
            )
        ),
        BundledWallpaper(
            id = "midnight_blue",
            name = "Cupertino Midnight",
            previewColor = Color(0xFF1E3A8A),
            backgroundBrush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF020617),
                    Color(0xFF0F172A),
                    Color(0xFF1E3A8A),
                    Color(0xFF0F172A),
                    Color(0xFF020617)
                )
            )
        ),
        BundledWallpaper(
            id = "obsidian",
            name = "Obsidian Glass",
            previewColor = Color(0xFF18181B),
            backgroundBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF09090B),
                    Color(0xFF18181B),
                    Color(0xFF27272A),
                    Color(0xFF09090B)
                ),
                start = Offset(0f, 0f),
                end = Offset(1080f, 2400f)
            )
        ),
        BundledWallpaper(
            id = "sunset_prism",
            name = "Sunset Prism",
            previewColor = Color(0xFFE11D48),
            backgroundBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF4C0519),
                    Color(0xFFBE123C),
                    Color(0xFFF97316),
                    Color(0xFF7C2D12),
                    Color(0xFF09090B)
                ),
                start = Offset(0f, 0f),
                end = Offset(1080f, 2400f)
            )
        ),
        BundledWallpaper(
            id = "emerald_glow",
            name = "Emerald Aurora",
            previewColor = Color(0xFF059669),
            backgroundBrush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF10B981),
                    Color(0xFF047857),
                    Color(0xFF064E3B),
                    Color(0xFF022C22),
                    Color(0xFF020617)
                ),
                center = Offset(500f, 600f),
                radius = 1100f
            )
        )
    )

    fun getWallpaper(id: String): BundledWallpaper {
        return bundledWallpapers.find { it.id == id } ?: bundledWallpapers[0]
    }
}

@Composable
fun WallpaperBackground(
    config: HomeScreenConfig,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hasCustom = !config.customWallpaperUri.isNullOrBlank()
    val blurModifier = if (config.wallpaperBlurDp > 0.5f) {
        Modifier.blur(config.wallpaperBlurDp.dp)
    } else Modifier

    Box(modifier = modifier.fillMaxSize()) {
        if (hasCustom) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(Uri.parse(config.customWallpaperUri))
                    .crossfade(true)
                    .build(),
                contentDescription = "Custom Wallpaper",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .then(blurModifier)
            )
        } else {
            val bundled = WallpaperProvider.getWallpaper(config.wallpaperId)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bundled.backgroundBrush)
                    .then(blurModifier)
            )
        }

        // Configurable dimming overlay for icon and text contrast
        if (config.wallpaperDim > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = config.wallpaperDim))
            )
        }
    }
}
