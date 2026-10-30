package com.example.launcher.ui.home

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.launcher.glass.LiquidGlassSurface
import com.example.launcher.model.AppFolder
import com.example.launcher.model.HomeScreenConfig
import com.example.launcher.model.LauncherApp

@Composable
fun FolderItem(
    folder: AppFolder,
    appsInFolder: List<LauncherApp>,
    getIconBitmap: (LauncherApp) -> Bitmap?,
    config: HomeScreenConfig,
    isEditMode: Boolean = false,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val baseIconSize = (60.dp * config.iconSizeScale)
    val shape = RoundedCornerShape(14.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.size(baseIconSize),
            contentAlignment = Alignment.Center
        ) {
            LiquidGlassSurface(
                modifier = Modifier
                    .size(baseIconSize)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    ),
                shape = shape,
                cornerRadius = 14.dp
            ) {
                // 3x3 Mini Grid of Apps inside the folder icon
                val previewApps = appsInFolder.take(9)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        for (row in 0 until 3) {
                            androidx.compose.foundation.layout.Row(
                                horizontalArrangement = Arrangement.spacedBy(2.5.dp)
                            ) {
                                for (col in 0 until 3) {
                                    val index = row * 3 + col
                                    val app = previewApps.getOrNull(index)
                                    if (app != null) {
                                        val bm = getIconBitmap(app)
                                        if (bm != null) {
                                            Image(
                                                bitmap = bm.asImageBitmap(),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(13.5.dp)
                                                    .clip(RoundedCornerShape(3.dp))
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(13.5.dp)
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(Color.White.copy(alpha = 0.3f))
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.size(13.5.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isEditMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(20.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(Color(0xFF71717A))
                        .clickable { onRemoveClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        if (config.showLabels) {
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = folder.name,
                color = Color.White,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.90f),
                        offset = Offset(0f, 1.5f),
                        blurRadius = 3.5f
                    )
                )
            )
        }
    }
}

/**
 * Full-screen iOS folder dialog overlay.
 */
@Composable
fun FolderExpandedDialog(
    folder: AppFolder,
    appsInFolder: List<LauncherApp>,
    getIconBitmap: (LauncherApp) -> Bitmap?,
    config: HomeScreenConfig,
    onAppClick: (LauncherApp) -> Unit,
    onRenameFolder: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var isRenaming by remember { mutableStateOf(false) }
    var folderNameInput by remember(folder.name) { mutableStateOf(folder.name) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(320.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {} // Consume click inside dialog
                )
        ) {
            // Folder title at the top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isRenaming) {
                    BasicTextField(
                        value = folderNameInput,
                        onValueChange = { folderNameInput = it },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        cursorBrush = SolidColor(Color.White),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                } else {
                    androidx.compose.foundation.layout.Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.clickable { isRenaming = true }
                    ) {
                        Text(
                            text = folderNameInput,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Rename",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Liquid Glass Folder Container
            LiquidGlassSurface(
                modifier = Modifier
                    .size(310.dp)
                    .shadow(16.dp, RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                cornerRadius = 32.dp
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(appsInFolder, key = { it.id }) { app ->
                        AppIconItem(
                            app = app,
                            iconBitmap = getIconBitmap(app),
                            config = config,
                            isEditMode = false,
                            onClick = {
                                onAppClick(app)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}
