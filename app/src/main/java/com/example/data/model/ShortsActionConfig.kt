package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Configuration item for customizable Shorts action icons.
 * Coordinates (xRatio, yRatio) are normalized fractions (0.0 .. 1.0) relative to viewport width/height,
 * enabling responsive, portrait/landscape, and screen-agnostic layout.
 */
data class ShortsActionConfig(
    val id: String,
    val name: String,
    val isVisible: Boolean = true,
    val xRatio: Float,
    val yRatio: Float,
    val isProtected: Boolean = false, // Protected controls cannot be deleted into the trash area
    val scale: Float = 1.0f
) {
    fun toSerialized(): String {
        return "$id|$name|$isVisible|$xRatio|$yRatio|$isProtected|$scale"
    }

    companion object {
        fun fromSerialized(str: String): ShortsActionConfig? {
            return try {
                val parts = str.split("|")
                if (parts.size >= 6) {
                    val sc = if (parts.size >= 7) parts[6].toFloatOrNull() ?: 1.0f else 1.0f
                    ShortsActionConfig(
                        id = parts[0],
                        name = parts[1],
                        isVisible = parts[2].toBoolean(),
                        xRatio = parts[3].toFloat(),
                        yRatio = parts[4].toFloat(),
                        isProtected = parts[5].toBoolean(),
                        scale = sc
                    )
                } else null
            } catch (_: Exception) {
                null
            }
        }
    }
}

object ShortsActionIds {
    const val LIKE = "like"
    const val DISLIKE = "dislike"
    const val COMMENT = "comment"
    const val SHARE = "share"
    const val REMIX = "remix"
    const val MORE_MENU = "more"
    const val SUBSCRIBE = "subscribe"
    const val SOUND_TRACK = "audio"
}

object ShortsActionDefaults {
    val DEFAULT_ACTIONS: List<ShortsActionConfig> = listOf(
        ShortsActionConfig(id = ShortsActionIds.LIKE, name = "Like", isVisible = true, xRatio = 0.86f, yRatio = 0.48f),
        ShortsActionConfig(id = ShortsActionIds.COMMENT, name = "Comment", isVisible = true, xRatio = 0.86f, yRatio = 0.58f),
        ShortsActionConfig(id = ShortsActionIds.SHARE, name = "Share", isVisible = true, xRatio = 0.86f, yRatio = 0.68f),
        ShortsActionConfig(id = ShortsActionIds.REMIX, name = "Remix", isVisible = true, xRatio = 0.86f, yRatio = 0.78f),
        ShortsActionConfig(id = ShortsActionIds.SOUND_TRACK, name = "Sound Track", isVisible = true, xRatio = 0.86f, yRatio = 0.88f),
        ShortsActionConfig(id = ShortsActionIds.SUBSCRIBE, name = "Subscribe", isVisible = true, xRatio = 0.45f, yRatio = 0.88f)
    )

    fun getDefaultActions(): List<ShortsActionConfig> = DEFAULT_ACTIONS
}

