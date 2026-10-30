package com.example.launcher.ui.search

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.launcher.glass.LiquidGlassSurface
import com.example.launcher.model.LauncherApp

@Composable
fun SpotlightSearchOverlay(
    isVisible: Boolean,
    allApps: List<LauncherApp>,
    recentSearches: List<String>,
    getIconBitmap: (LauncherApp) -> Bitmap?,
    onAppClick: (LauncherApp) -> Unit,
    onClearHistory: () -> Unit,
    onSearchSubmit: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    val searchResults = remember(query, allApps) {
        if (query.isBlank()) emptyList()
        else {
            val q = query.trim()
            allApps.filter {
                it.displayLabel.contains(q, ignoreCase = true) ||
                        it.packageName.contains(q, ignoreCase = true)
            }
        }
    }

    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
        ) {
            // Floating Liquid Glass Search Bar
            LiquidGlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                cornerRadius = 16.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (query.isEmpty()) {
                            Text(
                                text = "Search Apps & System...",
                                color = Color.White.copy(alpha = 0.45f),
                                fontSize = 16.sp
                            )
                        }
                        BasicTextField(
                            value = query,
                            onValueChange = {
                                query = it
                                if (it.isNotBlank()) onSearchSubmit(it)
                            },
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            cursorBrush = SolidColor(Color(0xFF3B82F6)),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                        )
                    }
                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = { query = "" },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search results or recent searches
            if (query.isNotBlank()) {
                Text(
                    text = "Top Hits (${searchResults.size})",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(searchResults, key = { it.id }) { app ->
                        LiquidGlassSurface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clickable {
                                    onAppClick(app)
                                    onDismiss()
                                },
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
                                        contentDescription = null,
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
                                        text = app.packageName,
                                        color = Color.White.copy(alpha = 0.45f),
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (recentSearches.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Searches",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Clear All",
                        color = Color(0xFF60A5FA),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { onClearHistory() }
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(recentSearches) { term ->
                        LiquidGlassSurface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clickable { query = term },
                            shape = RoundedCornerShape(12.dp),
                            cornerRadius = 12.dp
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = term,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
