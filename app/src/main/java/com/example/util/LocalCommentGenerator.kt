package com.example.util

import com.example.data.local.CommentEntity
import kotlin.math.abs

object LocalCommentGenerator {

    /**
     * Generates 12-15 natural, video-specific local comments based entirely on offline metadata.
     * No network, cloud APIs, or external ML services required.
     */
    fun generateLocalComments(
        videoId: String,
        title: String,
        channelName: String,
        durationMs: Long,
        isShort: Boolean,
        hashtags: String = ""
    ): List<CommentEntity> {
        val hash = abs(videoId.hashCode() * 31 + title.hashCode())
        val cleanTitle = title.take(50).trim()
        val durationMinutes = durationMs / 60000

        val authors = listOf(
            Triple("Jordan Lee", "@jordan_l", "J"),
            Triple("Taylor Vance", "@taylor_v", "T"),
            Triple("Samira Khan", "@samira_k", "S"),
            Triple("Devon Park", "@devon_p", "D"),
            Triple("Casey Miller", "@casey_m", "C"),
            Triple("Elena Rostova", "@elena_r", "E"),
            Triple("Marcus Chen", "@marcus_c", "M"),
            Triple("Avery Brooks", "@avery_b", "A"),
            Triple("Liam Bennett", "@liam_b", "L"),
            Triple("Sophia Patel", "@sophia_p", "S"),
            Triple("Noah Ramirez", "@noah_r", "N"),
            Triple("Chloe Diaz", "@chloe_d", "C"),
            Triple("Lucas Scott", "@lucas_s", "L"),
            Triple("Maya Lin", "@maya_l", "M"),
            Triple("Riley Morgan", "@riley_m", "R")
        )

        val commentPool = mutableListOf<String>()

        if (isShort) {
            commentPool.add("Awesome Short! Loved the quick pacing and edits.")
            commentPool.add("This clip is so satisfying to watch on loop 🔥")
            commentPool.add("Great moment captured here! Perfect length for a Short 👍")
            commentPool.add("Short and punchy! Definitely saving this one to offline favorites.")
            commentPool.add("The replay value on this clip is insane!")
            commentPool.add("That smooth transition caught me completely off guard.")
            commentPool.add("10/10 offline clip. Glad I kept this downloaded.")
            commentPool.add("Quality content from $channelName as always.")
            commentPool.add("Watching this on loop right now, brilliant timing!")
            commentPool.add("Super clean presentation for \"$cleanTitle\".")
            commentPool.add("Underrated Short! Deserves way more appreciation.")
            commentPool.add("Really creative editing on this sequence.")
            commentPool.add("Short, sweet, and to the point. Great local watch.")
            commentPool.add("Best clip I've seen all week!")
            commentPool.add("Love the audio rhythm paired with this visual pace.")
        } else {
            if (durationMinutes > 5) {
                commentPool.add("Really enjoyed watching all ${durationMinutes} minutes of this! High quality from start to finish.")
                commentPool.add("Great full-length local watch. The pacing kept me fully engaged throughout.")
                commentPool.add("Outstanding presentation by $channelName! Very detailed and informative.")
            } else {
                commentPool.add("Really well produced video, crisp clarity and great pacing.")
                commentPool.add("Enjoyed this video! Glad I saved it to my local library.")
                commentPool.add("Short, focused, and packed with interesting moments.")
            }
            commentPool.add("Great work on \"$cleanTitle\"! Really stands out in the library.")
            commentPool.add("The audio clarity and visual flow are top notch.")
            commentPool.add("Definitely keeping this saved in my offline downloads for travel.")
            commentPool.add("Fantastic presentation. Appreciate the detailed effort!")
            commentPool.add("One of the best local videos I have saved on this device.")
            commentPool.add("So convenient having this ready to watch offline without buffering.")
            commentPool.add("The chapter transitions and structure make this a pleasure to rewatch.")
            commentPool.add("Always happy to see new uploads from $channelName.")
            commentPool.add("Bookmarked and shared with a friend!")
            commentPool.add("Rewatching this today and it holds up just as well as the first time.")
            commentPool.add("Simple, effective, and very enjoyable watch.")
        }

        // Add tag-specific comments if tags exist
        if (hashtags.isNotBlank()) {
            val primaryTag = hashtags.split(" ").firstOrNull { it.startsWith("#") } ?: ""
            if (primaryTag.isNotEmpty()) {
                commentPool.add("Always looking for more $primaryTag content like this.")
                commentPool.add("Top quality $primaryTag video! Thanks for creating it.")
            }
        }

        // Select 12-14 distinct comments deterministically
        val targetCount = 13 + (hash % 3) // 13, 14, or 15
        val selectedComments = mutableListOf<String>()

        for (i in 0 until commentPool.size * 2) {
            val idx = (hash + i * 11) % commentPool.size
            val candidate = commentPool[idx]
            if (!selectedComments.contains(candidate)) {
                selectedComments.add(candidate)
            }
            if (selectedComments.size >= targetCount) break
        }

        val baseTime = System.currentTimeMillis() - 7200000L // 2 hours ago
        return selectedComments.mapIndexed { index, text ->
            val author = authors[index % authors.size]
            val timeOffset = (index * 3600000L * 4) + (hash % 1800000L) // staggered hours/days
            val initialLikes = if (index == 0) {
                42 + (hash % 35) // High likes for pinned/highlighted top comment
            } else {
                (28 - index * 2 + (hash % 5)).coerceAtLeast(1)
            }
            CommentEntity(
                videoId = videoId,
                authorName = author.first,
                authorHandle = author.second,
                authorAvatarLetter = author.third,
                text = text,
                timestamp = baseTime - timeOffset,
                likesCount = initialLikes,
                isLiked = false
            )
        }
    }
}

