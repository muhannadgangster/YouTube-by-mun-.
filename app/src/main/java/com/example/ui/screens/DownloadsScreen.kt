package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownloadDone
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VideoItem
import com.example.ui.components.VideoThumbnailView
import com.example.ui.theme.LocalAppAccentColor
import com.example.ui.theme.LocalLiquidGlassConfig
import com.example.ui.theme.YtBorder
import com.example.ui.theme.YtDarkBackground
import com.example.ui.theme.YtPillActive
import com.example.ui.theme.YtPillActiveText
import com.example.ui.theme.YtPillBackground
import com.example.ui.theme.YtRed
import com.example.ui.theme.YtSurfaceDark
import com.example.ui.theme.YtSurfaceVariant
import com.example.ui.theme.YtTextPrimary
import com.example.ui.theme.YtTextSecondary
import com.example.ui.theme.liquidGlassSurface
import java.util.Locale

/**
 * Dedicated Liquid Glass Downloads Screen (Accessible exclusively via Profile → Downloads):
 * - Displays real local downloaded videos with actual offline data
 * - Features rich 3D liquid glass surfaces, optical curvature, and blur
 * - Shows status (Downloaded / Ready offline), thumbnail, duration, and disk storage usage
 * - Functional playback, single-item removal, and bulk deletion management
 * - Handles empty and filled states smoothly
 */
@Composable
fun DownloadsScreen(
    downloadedVideos: List<VideoItem>,
    onVideoClick: (VideoItem) -> Unit,
    onRemoveDownload: (VideoItem) -> Unit,
    onDeleteAllDownloads: () -> Unit,
    onShareClick: (VideoItem) -> Unit = {},
    onBack: () -> Unit,
    onExploreVideos: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBack()
    }

    val glassConfig = LocalLiquidGlassConfig.current
    val accentColor = LocalAppAccentColor.current
    val isGlass = glassConfig.isEnabled

    var selectedFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    // Filter videos by category and search
    val filteredList = downloadedVideos.filter { item ->
        val matchesCategory = when (selectedFilter) {
            "Videos" -> !item.isShort
            "Shorts" -> item.isShort
            else -> true
        }
        val matchesSearch = searchQuery.isBlank() ||
                item.displayTitle.contains(searchQuery, ignoreCase = true) ||
                item.displayChannelName.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    // Calculate total downloaded storage size
    val totalBytes = downloadedVideos.sumOf { it.sizeBytes }
    val formattedTotalStorage = remember(totalBytes) {
        when {
            totalBytes >= 1_000_000_000L -> String.format(Locale.US, "%.1f GB", totalBytes / 1_000_000_000.0)
            totalBytes >= 1_000_000L -> String.format(Locale.US, "%.1f MB", totalBytes / 1_000_000.0)
            totalBytes >= 1_000L -> String.format(Locale.US, "%.1f KB", totalBytes / 1_000.0)
            else -> "$totalBytes B"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (isGlass) Color.Transparent else YtDarkBackground)
    ) {
        // 1. Top App Bar with Liquid Glass styling
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isGlass) {
                        Modifier.liquidGlassSurface(
                            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                            config = glassConfig,
                            accentColor = accentColor,
                            elevation = 8.dp
                        )
                    } else {
                        Modifier.background(YtSurfaceDark)
                    }
                )
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("btn_downloads_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Profile",
                        tint = YtTextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Downloads",
                        color = YtTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${downloadedVideos.size} videos • Ready offline",
                        color = accentColor,
                        fontSize = 11.sp
                    )
                }

                if (downloadedVideos.isNotEmpty()) {
                    IconButton(
                        onClick = { isSearchActive = !isSearchActive },
                        modifier = Modifier.testTag("btn_downloads_search_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Downloads",
                            tint = YtTextPrimary
                        )
                    }

                    IconButton(
                        onClick = { showDeleteAllDialog = true },
                        modifier = Modifier.testTag("btn_downloads_delete_all")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete All Downloads",
                            tint = YtTextSecondary
                        )
                    }
                }
            }
        }

        // 2. Main Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Storage Statistics Liquid Glass Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (isGlass) {
                                Modifier.liquidGlassSurface(
                                    shape = RoundedCornerShape(18.dp),
                                    config = glassConfig,
                                    accentColor = accentColor,
                                    elevation = 4.dp
                                )
                            } else {
                                Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(YtSurfaceDark)
                            }
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.20f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownloadDone,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Local Offline Storage",
                                color = YtTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$formattedTotalStorage used across ${downloadedVideos.size} items",
                                color = YtTextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF22C55E).copy(alpha = 0.20f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Available",
                                color = Color(0xFF22C55E),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Filter Chips
            if (downloadedVideos.isNotEmpty()) {
                item {
                    val filterChips = listOf("All", "Videos", "Shorts")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filterChips.forEach { chip ->
                            val isSelected = selectedFilter == chip
                            val shape = RoundedCornerShape(20.dp)
                            Box(
                                modifier = Modifier
                                    .then(
                                        if (isSelected) {
                                            Modifier
                                                .clip(shape)
                                                .background(if (isGlass) Color.White else YtPillActive)
                                        } else if (isGlass) {
                                            Modifier.liquidGlassSurface(
                                                shape = shape,
                                                config = glassConfig,
                                                elevation = 2.dp
                                            )
                                        } else {
                                            Modifier
                                                .clip(shape)
                                                .background(YtPillBackground)
                                        }
                                    )
                                    .clickable { selectedFilter = chip }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = chip,
                                    color = if (isSelected) YtPillActiveText else YtTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Empty State
            if (filteredList.isEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(30.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (isGlass) {
                                    Modifier.liquidGlassSurface(
                                        shape = RoundedCornerShape(20.dp),
                                        config = glassConfig,
                                        accentColor = accentColor,
                                        elevation = 6.dp
                                    )
                                } else {
                                    Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(YtSurfaceDark)
                                }
                            )
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(accentColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = if (downloadedVideos.isEmpty()) "No downloaded videos yet" else "No matching downloads found",
                                color = YtTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (downloadedVideos.isEmpty())
                                    "Save local videos from your home feed to watch anytime offline without network connectivity."
                                else "Try adjusting your search or category filter.",
                                color = YtTextSecondary,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 16.sp
                            )

                            if (downloadedVideos.isEmpty()) {
                                Spacer(modifier = Modifier.height(18.dp))
                                Button(
                                    onClick = {
                                        onBack()
                                        onExploreVideos()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    Text(
                                        text = "Explore Videos",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Downloaded Video Items List
                items(filteredList, key = { it.id }) { video ->
                    DownloadedVideoCard(
                        video = video,
                        isGlass = isGlass,
                        accentColor = accentColor,
                        onVideoClick = { onVideoClick(video) },
                        onRemove = { onRemoveDownload(video) },
                        onShare = { onShareClick(video) }
                    )
                }
            }
        }
    }

    // Delete All Confirmation Dialog
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Delete all downloads?", color = YtTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This will remove all ${downloadedVideos.size} items from your downloaded offline list.",
                    color = YtTextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteAllDownloads()
                        showDeleteAllDialog = false
                    }
                ) {
                    Text("Delete All", color = YtRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Cancel", color = YtTextPrimary)
                }
            },
            containerColor = YtSurfaceDark
        )
    }
}

@Composable
private fun DownloadedVideoCard(
    video: VideoItem,
    isGlass: Boolean,
    accentColor: Color,
    onVideoClick: () -> Unit,
    onRemove: () -> Unit,
    onShare: () -> Unit
) {
    val glassConfig = LocalLiquidGlassConfig.current
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isGlass) {
                    Modifier.liquidGlassSurface(
                        shape = RoundedCornerShape(16.dp),
                        config = glassConfig,
                        accentColor = accentColor,
                        elevation = 3.dp
                    )
                } else {
                    Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(YtSurfaceDark)
                }
            )
            .clickable { onVideoClick() }
            .padding(10.dp)
            .testTag("download_item_${video.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                VideoThumbnailView(
                    video = video,
                    modifier = Modifier.fillMaxSize()
                )

                // Duration badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.80f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = video.formattedDuration,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.displayTitle,
                    color = YtTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = video.displayChannelName,
                    color = YtTextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Downloaded",
                        tint = accentColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Downloaded • Ready offline",
                        color = accentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Menu button
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = YtTextSecondary
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Play") },
                        onClick = {
                            showMenu = false
                            onVideoClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Share") },
                        onClick = {
                            showMenu = false
                            onShare()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Share, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Remove from Downloads", color = YtRed) },
                        onClick = {
                            showMenu = false
                            onRemove()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = YtRed)
                        }
                    )
                }
            }
        }
    }
}
