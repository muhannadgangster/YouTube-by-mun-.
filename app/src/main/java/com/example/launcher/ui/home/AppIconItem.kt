package com.example.launcher.ui.home

import android.graphics.Bitmap
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.launcher.model.HomeScreenConfig
import com.example.launcher.model.LauncherApp

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AppIconItem(
    app: LauncherApp,
    iconBitmap: Bitmap?,
    config: HomeScreenConfig,
    isEditMode: Boolean = false,
    itemIndex: Int = 0,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onRemoveClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // iOS spring press-down scale
    val pressScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 400f),
        label = "press_scale"
    )

    // iOS Jiggle (wiggle) animation in edit mode with staggered phase per item index
    val jiggleAngle = if (isEditMode && !config.reducedMotion) {
        val transition = rememberInfiniteTransition(label = "jiggle")
        val delay = (itemIndex % 3) * 40
        val angle by transition.animateFloat(
            initialValue = -1.6f,
            targetValue = 1.6f,
            animationSpec = infiniteRepeatable(
                animation = tween(140 + delay, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "jiggle_angle"
        )
        angle
    } else 0f

    val baseIconSize = (60.dp * config.iconSizeScale)
    val shape = when (config.iconShape) {
        "CIRCLE" -> CircleShape
        "ROUNDED_SQUARE" -> RoundedCornerShape(12.dp)
        else -> RoundedCornerShape(14.dp) // Authentic iOS Squircle
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .rotate(jiggleAngle)
            .scale(pressScale)
    ) {
        Box(
            modifier = Modifier.size(baseIconSize),
            contentAlignment = Alignment.Center
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(baseIconSize)
                    .shadow(
                        elevation = if (isEditMode) 2.dp else 4.dp,
                        shape = shape,
                        clip = false
                    )
                    .clip(shape)
                    .background(Color(0xFF2A2A30))
                    .combinedClickable(
                        interactionSource = interactionSource,
                        indication = null, // Custom spring scale feedback
                        onClick = onClick,
                        onLongClick = onLongClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (iconBitmap != null) {
                    Image(
                        bitmap = iconBitmap.asImageBitmap(),
                        contentDescription = app.displayLabel,
                        modifier = Modifier.size(baseIconSize)
                    )
                } else {
                    // Fallback elegant iOS monogram
                    Text(
                        text = app.displayLabel.take(1).uppercase(),
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // iOS Edit Mode Minus "-" Badge
            if (isEditMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = (-4).dp, y = (-4).dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF71717A))
                        .clickable { onRemoveClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Remove",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        if (config.showLabels) {
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = app.displayLabel,
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
