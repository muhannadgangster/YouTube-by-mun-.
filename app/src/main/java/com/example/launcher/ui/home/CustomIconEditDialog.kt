package com.example.launcher.ui.home

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.launcher.glass.LiquidGlassSurface
import com.example.launcher.model.LauncherApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CustomIconEditDialog(
    app: LauncherApp,
    currentBitmap: Bitmap?,
    onSave: (newTitle: String?, customBitmap: Bitmap?, resetIcon: Boolean) -> Unit,
    onHideApp: (Boolean) -> Unit,
    onAppInfoClick: () -> Unit,
    onUninstallClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var editedTitle by remember { mutableStateOf(app.displayLabel) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var shouldResetIcon by remember { mutableStateOf(false) }

    // Android Photo Picker launcher (zero-permission)
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val loaded = withContext(Dispatchers.IO) {
                    try {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            val decoded = BitmapFactory.decodeStream(stream)
                            if (decoded != null) {
                                // Crop to 1:1 center square for iOS icon
                                val minDim = minOf(decoded.width, decoded.height)
                                val x = (decoded.width - minDim) / 2
                                val y = (decoded.height - minDim) / 2
                                val square = Bitmap.createBitmap(decoded, x, y, minDim, minDim)
                                Bitmap.createScaledBitmap(square, 192, 192, true)
                            } else null
                        }
                    } catch (_: Exception) {
                        null
                    }
                }
                if (loaded != null) {
                    selectedBitmap = loaded
                    shouldResetIcon = false
                }
            }
        }
    }

    val previewBitmap = when {
        selectedBitmap != null -> selectedBitmap
        shouldResetIcon -> null
        currentBitmap != null -> currentBitmap
        else -> null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.70f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        LiquidGlassSurface(
            modifier = Modifier
                .width(340.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {} // Consume inner click
                ),
            shape = RoundedCornerShape(28.dp),
            cornerRadius = 28.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Customize App",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Squircle App Icon Preview
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF26262B))
                        .border(1.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
                        .clickable {
                            photoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (previewBitmap != null) {
                        Image(
                            bitmap = previewBitmap.asImageBitmap(),
                            contentDescription = "App Icon Preview",
                            modifier = Modifier.size(80.dp)
                        )
                    } else {
                        Text(
                            text = editedTitle.take(1).uppercase(),
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Gallery Picker & Reset buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            photoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF60A5FA)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Gallery Photo", fontSize = 12.sp)
                    }

                    if (app.customIconUri != null || selectedBitmap != null) {
                        OutlinedButton(
                            onClick = {
                                selectedBitmap = null
                                shouldResetIcon = true
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFF87171)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Editable Name Text Field
                OutlinedTextField(
                    value = editedTitle,
                    onValueChange = { editedTitle = it },
                    label = { Text("App Name", color = Color.White.copy(alpha = 0.7f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Quick Action Rows
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { onAppInfoClick() }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("App Info & Details", color = Color.White, fontSize = 13.5.sp)
                    }
                    Text("Settings >", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEF4444).copy(alpha = 0.12f))
                        .clickable { onUninstallClick() }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = Color(0xFFF87171),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Uninstall App", color = Color(0xFFF87171), fontSize = 13.5.sp)
                    }
                    Text("Android System >", color = Color(0xFFF87171).copy(alpha = 0.6f), fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Save Button
                Button(
                    onClick = {
                        val finalTitle = if (editedTitle.trim() == app.label) null else editedTitle.trim()
                        onSave(finalTitle, selectedBitmap, shouldResetIcon)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563EB)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Apply Changes", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
