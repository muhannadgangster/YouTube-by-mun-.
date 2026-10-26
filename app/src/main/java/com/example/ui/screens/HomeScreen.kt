package com.example.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VideoItem
import com.example.ui.components.HomeShortsShelf
import com.example.ui.components.VideoFeedCard
import com.example.ui.components.VideoThumbnailView
import com.example.ui.theme.LocalAppAccentColor
import com.example.ui.theme.LocalLiquidGlassConfig
import com.example.ui.theme.liquidGlassSurface
import com.example.ui.theme.YtBorder
import com.example.ui.theme.YtDarkBackground
import com.example.ui.theme.YtPillActive
import com.example.ui.theme.YtPillActiveText
import com.example.ui.theme.YtPillBackground
import com.example.ui.theme.YtRed
import com.example.ui.theme.YtSurfaceVariant
import com.example.ui.theme.YtTextPrimary
import com.example.ui.theme.YtTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    videos: List<VideoItem>,
    shortsList: List<VideoItem> = emptyList(),
    selectedFilter: String,
    isRefreshing: Boolean = false,
    isScanningMedia: Boolean = false,
    onRefresh: () -> Unit = {},
    onFilterSelected: (String) -> Unit,
    onVideoClick: (VideoItem) -> Unit,
    onShortClick: (VideoItem) -> Unit = onVideoClick,
    onEditTitleClick: (VideoItem) -> Unit,
    onToggleWatchLater: (VideoItem) -> Unit,
    onToggleSubscribe: (VideoItem) -> Unit,
    onToggleDownload: (VideoItem) -> Unit = {},
    onShareClick: (VideoItem) -> Unit,
    onScanStorageClick: () -> Unit,
    shortsGridCount: Int = 4,
    listState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState(),
    modifier: Modifier = Modifier
) {
    val filterChips = listOf("All", "Videos", "Shorts", "Downloaded", "Favorites")

    val appAccentColor = LocalAppAccentColor.current
    val glassConfig = LocalLiquidGlassConfig.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // Filter Chips Row with authentic Liquid Glass refraction and diamond rim
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Explore Compass Pill
            LiquidGlassFilterChip(
                icon = Icons.Default.Explore,
                isSelected = selectedFilter == "All",
                onClick = { onFilterSelected("All") },
                testTag = "filter_chip_explore"
            )

            // Quick Mix / Shuffle Chip
            LiquidGlassFilterChip(
                text = "Mix 🔀",
                icon = Icons.Default.Shuffle,
                isSelected = false,
                onClick = { onRefresh() },
                testTag = "filter_chip_mix_shuffle"
            )

            filterChips.forEach { chip ->
                LiquidGlassFilterChip(
                    text = chip,
                    isSelected = selectedFilter == chip,
                    onClick = { onFilterSelected(chip) },
                    testTag = "filter_chip_$chip"
                )
            }
        }

        // Pull to Refresh Box (Auto-shuffles and rescans videos on every pull-down refresh)
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            if (isScanningMedia && videos.isEmpty()) {
                // Scanning state: Scanning your gallery...
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = YtRed,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Scanning your gallery...",
                            color = YtTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Discovering videos from device storage",
                            color = YtTextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else if (videos.isEmpty()) {
                // Scan completed + no videos
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = null,
                            tint = YtTextSecondary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No videos found",
                            color = YtTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Pull down to scan your gallery or refresh.",
                            color = YtTextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onRefresh,
                            colors = ButtonDefaults.buttonColors(containerColor = YtSurfaceVariant)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = YtTextPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Rescan Gallery 🔄", color = YtTextPrimary)
                        }
                    }
                }
            } else {
                val effectiveShorts = if (shortsList.isNotEmpty()) shortsList else videos.filter { it.isShort }
                val regularVideos = if (videos.any { !it.isShort }) videos.filter { !it.isShort } else videos

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("home_videos_list"),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 86.dp)
                ) {
                    if (selectedFilter == "Shorts") {
                        // User selected "Shorts" filter chip: Show continuous shorts grid
                        item(key = "shorts_only_grid") {
                            HomeShortsShelf(
                                shortsList = effectiveShorts,
                                onShortClick = onShortClick,
                                gridCount = shortsGridCount
                            )
                        }
                    } else if (selectedFilter == "Videos") {
                        // User selected "Videos" filter chip: Show long videos with continuous cycle
                        val repeatCycles = 200
                        items(count = regularVideos.size * repeatCycles, key = { index -> "video_${index}_${regularVideos[index % regularVideos.size].id}" }) { index ->
                            val video = regularVideos[index % regularVideos.size]
                            VideoFeedCard(
                                video = video,
                                onClick = { onVideoClick(video) },
                                onEditTitleClick = { onEditTitleClick(video) },
                                onToggleWatchLater = { onToggleWatchLater(video) },
                                onToggleSubscribe = { onToggleSubscribe(video) },
                                onToggleDownload = { onToggleDownload(video) },
                                onShareClick = { onShareClick(video) }
                            )
                        }
                    } else {
                        // "All" / General feed (YouTube Layout requested by User):
                        // 1. TOP OF HOMEPAGE: Immediately show top Shorts shelf with configured grid count
                        if (effectiveShorts.isNotEmpty()) {
                            item(key = "top_shorts_shelf_initial") {
                                val topShorts = generateSequence { effectiveShorts }.flatten().take(shortsGridCount).toList()
                                HomeShortsShelf(
                                    shortsList = topShorts,
                                    onShortClick = onShortClick,
                                    gridCount = shortsGridCount
                                )
                            }
                        }

                        // 2. INFINITE SCROLL: 2 Videos, then Shorts grid section, looping seamlessly
                        val virtualCycles = 500
                        val videoBlockSize = 2
                        val videoCount = if (regularVideos.isNotEmpty()) regularVideos.size else 1
                        val totalVirtualBlocks = ((videoCount * virtualCycles) / videoBlockSize).coerceAtLeast(50)

                        for (blockIndex in 0 until totalVirtualBlocks) {
                            // 2 Videos block
                            items(
                                count = videoBlockSize,
                                key = { itemIdx ->
                                    val actualIdx = (blockIndex * videoBlockSize + itemIdx) % regularVideos.size
                                    "inf_video_block_${blockIndex}_item_${itemIdx}_${regularVideos[actualIdx].id}"
                                }
                            ) { itemIdx ->
                                val actualIdx = (blockIndex * videoBlockSize + itemIdx) % regularVideos.size
                                val video = regularVideos[actualIdx]
                                VideoFeedCard(
                                    video = video,
                                    onClick = { onVideoClick(video) },
                                    onEditTitleClick = { onEditTitleClick(video) },
                                    onToggleWatchLater = { onToggleWatchLater(video) },
                                    onToggleSubscribe = { onToggleSubscribe(video) },
                                    onToggleDownload = { onToggleDownload(video) },
                                    onShareClick = { onShareClick(video) }
                                )
                            }

                            // Followed by Shorts Shelf (configurable 2, 4, 6, 8 grid)
                            if (effectiveShorts.isNotEmpty()) {
                                item(key = "inf_shorts_shelf_after_block_$blockIndex") {
                                    val shelfShorts = generateSequence { effectiveShorts }
                                        .flatten()
                                        .drop((blockIndex + 1) * shortsGridCount)
                                        .take(shortsGridCount)
                                        .toList()
                                    HomeShortsShelf(
                                        shortsList = shelfShorts,
                                        onShortClick = onShortClick,
                                        gridCount = shortsGridCount
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Reusable LiquidGlassFilterChip:
 * - Real optical background refraction and diamond edge bevel
 * - Smooth tactile press animation with refraction bloom
 * - Foreground text and icons stay 100% sharp and legible
 */
@Composable
private fun LiquidGlassFilterChip(
    text: String? = null,
    icon: ImageVector? = null,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    testTag: String,
    accentColor: Color = LocalAppAccentColor.current,
    modifier: Modifier = Modifier
) {
    val glassConfig = LocalLiquidGlassConfig.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth tactile scale & refraction bloom on touch/press
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "chip_scale"
    )

    val chipShape = RoundedCornerShape(20.dp)

    val chipConfig = remember(glassConfig, isSelected, isPressed) {
        glassConfig.copy(
            transparency = if (isSelected) 0.82f else if (isPressed) 0.76f else 0.92f,
            refractionAmount = if (isPressed) (glassConfig.refractionAmount * 1.5f).coerceAtMost(1f) else (if (isSelected) (glassConfig.refractionAmount * 1.25f).coerceAtMost(1f) else glassConfig.refractionAmount),
            lensMagnification = if (isSelected) 1.18f else 1.0f,
            edgeHighlights = if (isSelected) 0.95f else glassConfig.edgeHighlights,
            cornerRadiusDp = 20f
        )
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(chipShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        // Dedicated Background Glass Layer with optical refraction & diamond rim
        if (glassConfig.isEnabled) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .liquidGlassSurface(
                        shape = chipShape,
                        config = chipConfig,
                        accentColor = if (isSelected) accentColor else Color.White,
                        elevation = if (isSelected) 4.dp else 2.dp
                    )
                    .border(
                        width = if (isSelected) 1.2.dp else 0.8.dp,
                        brush = Brush.linearGradient(
                            colors = if (isSelected) listOf(
                                Color.White.copy(alpha = 0.95f),
                                accentColor.copy(alpha = 0.65f),
                                Color.White.copy(alpha = 0.85f)
                            ) else listOf(
                                Color.White.copy(alpha = 0.50f),
                                Color.White.copy(alpha = 0.15f),
                                Color.White.copy(alpha = 0.40f)
                            )
                        ),
                        shape = chipShape
                    )
            )
        } else {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(if (isSelected) YtPillActive else YtPillBackground)
                    .border(0.75.dp, if (isSelected) accentColor else YtBorder, chipShape)
            )
        }

        // Foreground: Razor-sharp text and icon
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = if (isSelected) (if (glassConfig.isEnabled) Color.White else Color.Black) else YtTextPrimary,
                    modifier = Modifier.size(16.dp)
                )
                if (text != null) {
                    Spacer(modifier = Modifier.width(5.dp))
                }
            }
            if (text != null) {
                Text(
                    text = text,
                    color = if (isSelected) (if (glassConfig.isEnabled) Color.White else Color.Black) else YtTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}
