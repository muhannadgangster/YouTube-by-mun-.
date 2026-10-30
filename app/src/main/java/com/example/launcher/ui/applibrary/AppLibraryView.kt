package com.example.launcher.ui.applibrary

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.launcher.glass.LiquidGlassSurface
import com.example.launcher.model.AppCategory
import com.example.launcher.model.HomeScreenConfig
import com.example.launcher.model.LauncherApp
import com.example.launcher.ui.home.AppIconItem

@Composable
fun AppLibraryView(
    allApps: List<LauncherApp>,
    getIconBitmap: (LauncherApp) -> Bitmap?,
    config: HomeScreenConfig,
    onAppClick: (LauncherApp) -> Unit,
    onAppLongClick: (LauncherApp) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var expandedCategory by remember { mutableStateOf<AppCategory?>(null) }

    val isSearching = searchQuery.isNotBlank()
    val filteredApps = if (isSearching) {
        allApps.filter {
            it.displayLabel.contains(searchQuery.trim(), ignoreCase = true) ||
                    it.packageName.contains(searchQuery.trim(), ignoreCase = true)
        }
    } else emptyList()

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // App Library Search Bar
        LiquidGlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(14.dp),
            cornerRadius = 14.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "App Library",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 15.sp
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 15.sp
                        ),
                        cursorBrush = SolidColor(Color(0xFF3B82F6)),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { searchQuery = "" },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (isSearching) {
            // Alphabetical Instant Search List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredApps, key = { it.id }) { app ->
                    LiquidGlassSurface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickable { onAppClick(app) },
                        shape = RoundedCornerShape(14.dp),
                        cornerRadius = 14.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp)
                        ) {
                            val bm = getIconBitmap(app)
                            if (bm != null) {
                                Image(
                                    bitmap = bm.asImageBitmap(),
                                    contentDescription = app.displayLabel,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF33333E)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = app.displayLabel.take(1).uppercase(),
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = app.displayLabel,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = app.category.displayName,
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Categorized 2x2 Pods
            val categoriesWithApps = AppCategory.values().mapNotNull { category ->
                val apps = if (category == AppCategory.SUGGESTIONS) {
                    allApps.take(4)
                } else {
                    allApps.filter { it.category == category }
                }
                if (apps.isNotEmpty()) category to apps else null
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 100.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(categoriesWithApps, key = { it.first.name }) { (category, apps) ->
                    CategoryPodCard(
                        category = category,
                        apps = apps,
                        getIconBitmap = getIconBitmap,
                        config = config,
                        onAppClick = onAppClick,
                        onExpand = { expandedCategory = category }
                    )
                }
            }
        }
    }

    // Expanded Category Full View Dialog
    if (expandedCategory != null) {
        val cat = expandedCategory!!
        val catApps = if (cat == AppCategory.SUGGESTIONS) allApps.take(8) else allApps.filter { it.category == cat }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable { expandedCategory = null },
            contentAlignment = Alignment.Center
        ) {
            LiquidGlassSurface(
                modifier = Modifier
                    .width(320.dp)
                    .height(400.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    ),
                shape = RoundedCornerShape(28.dp),
                cornerRadius = 28.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
                    Text(
                        text = cat.displayName,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(catApps, key = { it.id }) { app ->
                            AppIconItem(
                                app = app,
                                iconBitmap = getIconBitmap(app),
                                config = config,
                                onClick = {
                                    onAppClick(app)
                                    expandedCategory = null
                                },
                                onLongClick = { onAppLongClick(app) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryPodCard(
    category: AppCategory,
    apps: List<LauncherApp>,
    getIconBitmap: (LauncherApp) -> Bitmap?,
    config: HomeScreenConfig,
    onAppClick: (LauncherApp) -> Unit,
    onExpand: () -> Unit
) {
    LiquidGlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .height(168.dp),
        shape = RoundedCornerShape(24.dp),
        cornerRadius = 24.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 2x2 grid inside pod: 3 full icons + 1 mini cluster
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                apps.getOrNull(0)?.let { app ->
                    PodIcon(app, getIconBitmap(app), onClick = { onAppClick(app) })
                }
                apps.getOrNull(1)?.let { app ->
                    PodIcon(app, getIconBitmap(app), onClick = { onAppClick(app) })
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                apps.getOrNull(2)?.let { app ->
                    PodIcon(app, getIconBitmap(app), onClick = { onAppClick(app) })
                }
                // 4th slot: either app or mini cluster
                if (apps.size > 3) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .clickable { onExpand() }
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // 2x2 miniature icons
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                MiniIcon(apps.getOrNull(3), getIconBitmap)
                                MiniIcon(apps.getOrNull(4), getIconBitmap)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                MiniIcon(apps.getOrNull(5), getIconBitmap)
                                MiniIcon(apps.getOrNull(6), getIconBitmap)
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.size(52.dp))
                }
            }

            Text(
                text = category.displayName,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PodIcon(
    app: LauncherApp,
    bitmap: Bitmap?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF26262C))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = app.displayLabel,
                modifier = Modifier.size(52.dp)
            )
        } else {
            Text(
                text = app.displayLabel.take(1).uppercase(),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MiniIcon(
    app: LauncherApp?,
    getIconBitmap: (LauncherApp) -> Bitmap?
) {
    if (app != null) {
        val bm = getIconBitmap(app)
        if (bm != null) {
            Image(
                bitmap = bm.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(19.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(19.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.3f))
            )
        }
    } else {
        Spacer(modifier = Modifier.size(19.dp))
    }
}
