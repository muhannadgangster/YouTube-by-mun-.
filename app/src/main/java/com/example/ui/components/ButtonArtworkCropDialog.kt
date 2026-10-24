package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun ButtonArtworkCropDialog(
    imageUri: Uri,
    elementId: String,
    elementName: String,
    onDismiss: () -> Unit,
    onArtworkSaved: (savedPath: String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var scale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(imageUri) {
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(imageUri)?.use { stream ->
                    val opts = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    val tempBytes = stream.readBytes()
                    BitmapFactory.decodeByteArray(tempBytes, 0, tempBytes.size, opts)

                    var inSample = 1
                    val maxDim = maxOf(opts.outWidth, opts.outHeight)
                    while (maxDim / inSample > 1200) {
                        inSample *= 2
                    }

                    val decodeOpts = BitmapFactory.Options().apply {
                        inSampleSize = inSample
                    }
                    sourceBitmap = BitmapFactory.decodeByteArray(tempBytes, 0, tempBytes.size, decodeOpts)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF161820),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Customize Button Artwork",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = elementName,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF5252)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("crop_dialog_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Pinch or drag image to adjust framing within button mask",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Crop Viewport with Circular Mask Overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .aspectRatio(1.0f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0C0E14))
                        .border(1.dp, Color(0xFF2A2E3D), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (sourceBitmap != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        scale = (scale * zoom).coerceIn(0.5f, 3.5f)
                                        panOffset = Offset(
                                            x = panOffset.x + pan.x,
                                            y = panOffset.y + pan.y
                                        )
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = sourceBitmap!!.asImageBitmap(),
                                contentDescription = "Cropped Preview",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        translationX = panOffset.x
                                        translationY = panOffset.y
                                    }
                            )
                        }

                        // Circular Target Button Mask
                        Box(
                            modifier = Modifier
                                .fillMaxSize(0.72f)
                                .aspectRatio(1f)
                                .border(2.5.dp, Color(0xFFFF0033), CircleShape)
                        )
                    } else {
                        Text(
                            text = if (isLoading) "Loading image..." else "Could not load image",
                            color = Color.LightGray,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Zoom controls slider and buttons
                Row(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = { scale = (scale - 0.2f).coerceIn(0.5f, 3.5f) },
                        modifier = Modifier.testTag("crop_zoom_out")
                    ) {
                        Icon(Icons.Default.Remove, "Zoom Out", tint = Color.White)
                    }

                    Slider(
                        value = scale,
                        onValueChange = { scale = it },
                        valueRange = 0.5f..3.5f,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("crop_scale_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFF0033),
                            activeTrackColor = Color(0xFFFF0033)
                        )
                    )

                    IconButton(
                        onClick = { scale = (scale + 0.2f).coerceIn(0.5f, 3.5f) },
                        modifier = Modifier.testTag("crop_zoom_int")
                    ) {
                        Icon(Icons.Default.Add, "Zoom In", tint = Color.White)
                    }

                    IconButton(
                        onClick = {
                            scale = 1.0f
                            panOffset = Offset.Zero
                        },
                        modifier = Modifier.testTag("crop_reset_transform")
                    ) {
                        Icon(Icons.Default.Refresh, "Reset Transform", tint = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Live Button Shape Preview
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E212B), RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Button Preview: ",
                        color = Color.LightGray,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2A2D3A))
                            .border(1.5.dp, Color(0xFFFF0033), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (sourceBitmap != null) {
                            Image(
                                bitmap = sourceBitmap!!.asImageBitmap(),
                                contentDescription = "Small button preview",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        translationX = panOffset.x * 0.25f
                                        translationY = panOffset.y * 0.25f
                                    }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Action Buttons: Cancel and Apply
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("crop_dialog_cancel"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val bmp = sourceBitmap ?: return@Button
                            coroutineScope.launch {
                                val savedPath = saveCroppedBitmap(
                                    context = context,
                                    elementId = elementId,
                                    source = bmp,
                                    scale = scale,
                                    panOffset = panOffset
                                )
                                if (savedPath != null) {
                                    onArtworkSaved(savedPath)
                                }
                            }
                        },
                        enabled = sourceBitmap != null,
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("crop_dialog_apply"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0033))
                    ) {
                        Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Apply to App", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private suspend fun saveCroppedBitmap(
    context: Context,
    elementId: String,
    source: Bitmap,
    scale: Float,
    panOffset: Offset
): String? = withContext(Dispatchers.IO) {
    try {
        val targetSize = 256
        val result = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // Draw transformed source onto 256x256 square
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        canvas.save()
        // Center of target
        val cx = targetSize / 2f
        val cy = targetSize / 2f

        canvas.translate(cx + panOffset.x * 0.5f, cy + panOffset.y * 0.5f)
        canvas.scale(scale, scale)

        // Draw source centered
        val srcW = source.width.toFloat()
        val srcH = source.height.toFloat()
        val baseScale = targetSize / minOf(srcW, srcH)
        val drawW = srcW * baseScale
        val drawH = srcH * baseScale

        val dstRect = RectF(-drawW / 2f, -drawH / 2f, drawW / 2f, drawH / 2f)
        canvas.drawBitmap(source, null, dstRect, paint)
        canvas.restore()

        // Write directly to context.filesDir
        val safeId = elementId.replace(Regex("[^a-zA-Z0-9_]"), "_")
        val file = File(context.filesDir, "button_art_${safeId}.png")
        if (file.exists()) {
            file.delete()
        }
        FileOutputStream(file).use { out ->
            result.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
        }
        result.recycle()
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
