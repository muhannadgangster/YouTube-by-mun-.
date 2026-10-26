package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VideoItem
import com.example.ui.theme.LocalAppAccentColor
import com.example.ui.theme.LocalLiquidGlassConfig
import com.example.ui.theme.liquidGlassSurface
import com.example.ui.theme.YtRed
import com.example.ui.theme.YtSurfaceDark
import com.example.ui.theme.YtTextPrimary
import com.example.ui.theme.YtTextSecondary

/**
 * YouTube Home Feed Shorts Shelf Component (Matches HomeShortsShelf specification):
 * - Shelf Section Title with YouTube Shorts badge
 * - 2-Column Grid Layout for Shorts cards
 * - 9:16 aspect ratio vertical thumbnails with rounded corners
 * - Title overlay with bottom gradient scrim
 * - Context menu button (⋮ three dots) on top-right corner
 * - Tapping any card opens the full-screen vertical swipeable Shorts player
 */
@Composable
fun HomeShortsShelf(
    shortsList: List<VideoItem>,
    onShortClick: (VideoItem) -> Unit,
    gridCount: Int = 4,
    modifier: Modifier = Modifier
) {
    if (shortsList.isEmpty()) return
    val accentColor = LocalAppAccentColor.current
    val glassConfig = LocalLiquidGlassConfig.current

    val shelfShape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("home_shorts_shelf")
    ) {
        // Dedicated Background Liquid Glass Layer for Shorts Shelf
        if (glassConfig.isEnabled) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .liquidGlassSurface(
                        shape = shelfShape,
                        config = glassConfig,
                        accentColor = accentColor,
                        elevation = 4.dp
                    )
                    .border(
                        width = 0.8.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.50f),
                                Color.White.copy(alpha = 0.12f),
                                Color.White.copy(alpha = 0.35f)
                            )
                        ),
                        shape = shelfShape
                    )
            )
        } else {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(shelfShape)
                    .background(Color(0x33000000))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            // Shelf Header: Shorts S-icon / Logo + "Shorts" title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shorts Icon Badge with customizable accent color
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚡",
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Shorts",
                    color = YtTextPrimary,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "›",
                    color = YtTextSecondary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Configurable Grid Layout (2, 4, 6, or 8 cards arranged in responsive pairs)
            val countToDisplay = if (gridCount in listOf(2, 4, 6, 8)) gridCount else 4
            val displayShorts = shortsList.take(countToDisplay)
            displayShorts.chunked(2).forEachIndexed { rowIndex, pair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    pair.forEach { shortItem ->
                        ShortGridCard(
                            short = shortItem,
                            onClick = { onShortClick(shortItem) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Balance row if odd count
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * Individual 9:16 Short Card with Outer Liquid Glass Border & Bevel
 */
@Composable
private fun ShortGridCard(
    short: VideoItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glassConfig = LocalLiquidGlassConfig.current
    val isGlass = glassConfig.isEnabled
    val cardShape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .aspectRatio(9f / 16f)
            .clickable { onClick() }
            .testTag("short_card_${short.id}")
    ) {
        // Outer liquid glass frame behind and around thumbnail borders
        if (isGlass) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .liquidGlassSurface(
                        shape = cardShape,
                        config = glassConfig,
                        elevation = 4.dp
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.65f),
                                Color.White.copy(alpha = 0.15f),
                                Color.White.copy(alpha = 0.45f)
                            )
                        ),
                        shape = cardShape
                    )
            )
        } else {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(cardShape)
                    .background(YtSurfaceDark)
                    .border(0.75.dp, Color.White.copy(alpha = 0.15f), cardShape)
            )
        }

        // Inner Video Thumbnail with rounded padding, leaving curved glass frame and bevel visible around it
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isGlass) 3.5.dp else 0.dp)
                .clip(RoundedCornerShape(13.dp))
        ) {
            // 9:16 Vertical Video Thumbnail
            VideoThumbnailView(
                video = short,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

        // Bottom dark gradient scrim for readable title overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color(0x99000000),
                            Color(0xEE000000)
                        )
                    )
                )
        )

        // Three Dots Menu on top-right corner
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(28.dp)
                .clip(CircleShape)
                .background(Color(0x66000000))
                .clickable { /* context options */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Options",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }

        // Title and Views Overlay at bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 10.dp, vertical = 10.dp)
        ) {
            Text(
                text = short.displayTitle,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = short.viewsCount,
                color = Color(0xCCFFFFFF),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
}
