package com.example.data.model

/**
 * Universal element configuration model for Complete App Customization Mode.
 * Covers all supported interactive elements across Home Feed, Shorts, Player,
 * Profile, Subscriptions, Search, Notifications, and Navigation.
 */
data class AppElementConfig(
    val id: String,
    val name: String,
    val screen: String, // "Shorts", "Home", "Player", "Profile", "Subscriptions", "Search", "Notifications", "Navigation"
    val isVisible: Boolean = true,
    val xRatio: Float,
    val yRatio: Float,
    val scale: Float = 1.0f,
    val isProtected: Boolean = false,
    val customArtworkPath: String? = null
) {
    fun toSerialized(): String {
        val art = customArtworkPath ?: ""
        return "$id|$name|$screen|$isVisible|$xRatio|$yRatio|$scale|$isProtected|$art"
    }

    companion object {
        fun fromSerialized(str: String): AppElementConfig? {
            return try {
                val parts = str.split("|")
                if (parts.size >= 8) {
                    val art = if (parts.size >= 9 && parts[8].isNotBlank()) parts[8] else null
                    AppElementConfig(
                        id = parts[0],
                        name = parts[1],
                        screen = parts[2],
                        isVisible = parts[3].toBoolean(),
                        xRatio = parts[4].toFloat(),
                        yRatio = parts[5].toFloat(),
                        scale = parts[6].toFloatOrNull() ?: 1.0f,
                        isProtected = parts[7].toBoolean(),
                        customArtworkPath = art
                    )
                } else null
            } catch (_: Exception) {
                null
            }
        }
    }
}

object AppElementCatalog {
    // Unique Element IDs
    const val SHORTS_LIKE = "like"
    const val SHORTS_DISLIKE = "dislike"
    const val SHORTS_COMMENT = "comment"
    const val SHORTS_SHARE = "share"
    const val SHORTS_REMIX = "remix"
    const val SHORTS_SUBSCRIBE = "subscribe"
    const val SHORTS_SOUND = "audio"
    const val SHORTS_MORE = "more"

    const val HOME_SEARCH = "home_search"
    const val HOME_NOTIFICATIONS = "home_notifications"
    const val HOME_PROFILE = "home_profile"
    const val HOME_SUBSCRIBE = "home_subscribe"

    const val PLAYER_LIKE = "player_like"
    const val PLAYER_SHARE = "player_share"
    const val PLAYER_DOWNLOAD = "player_download"
    const val PLAYER_SUBSCRIBE = "player_subscribe"
    const val PLAYER_SKIP_BACK = "player_skip_back"
    const val PLAYER_SKIP_FWD = "player_skip_fwd"
    const val PLAYER_MORE = "player_more"

    const val PROFILE_AVATAR = "profile_avatar"
    const val PROFILE_SETTINGS = "profile_settings"
    const val PROFILE_EDIT = "profile_edit"
    const val PROFILE_DOWNLOADS = "profile_downloads"

    const val NAV_HOME = "nav_home"
    const val NAV_SHORTS = "nav_shorts"
    const val NAV_PLUS = "nav_plus"
    const val NAV_SUBSCRIPTIONS = "nav_subscriptions"
    const val NAV_YOU = "nav_you"

    const val ART_HEART = "heart"
    const val ART_THUMB = "thumb"

    fun getAllDefaultElements(): List<AppElementConfig> = listOf(
        // Shorts Action Column & Controls
        AppElementConfig(SHORTS_LIKE, "Shorts Like", "Shorts", true, 0.86f, 0.46f, 1.0f),
        AppElementConfig(SHORTS_COMMENT, "Shorts Comment", "Shorts", true, 0.86f, 0.56f, 1.0f),
        AppElementConfig(SHORTS_SHARE, "Shorts Share", "Shorts", true, 0.86f, 0.66f, 1.0f),
        AppElementConfig(SHORTS_REMIX, "Shorts Remix", "Shorts", true, 0.86f, 0.76f, 1.0f),
        AppElementConfig(SHORTS_SOUND, "Shorts Sound Track", "Shorts", true, 0.86f, 0.86f, 1.0f),
        AppElementConfig(SHORTS_SUBSCRIBE, "Shorts Subscribe", "Shorts", true, 0.50f, 0.88f, 1.0f),

        // Home
        AppElementConfig(HOME_SEARCH, "Home Search Bar", "Home", true, 0.72f, 0.04f, 1.0f),
        AppElementConfig(HOME_NOTIFICATIONS, "Home Notifications Bell", "Home", true, 0.86f, 0.04f, 1.0f),
        AppElementConfig(HOME_PROFILE, "Home Profile Avatar", "Home", true, 0.94f, 0.04f, 1.0f),
        AppElementConfig(HOME_SUBSCRIBE, "Home Card Subscribe", "Home", true, 0.80f, 0.62f, 1.0f),

        // Player
        AppElementConfig(PLAYER_LIKE, "Player Like / Dislike", "Player", true, 0.20f, 0.62f, 1.0f),
        AppElementConfig(PLAYER_SHARE, "Player Share Button", "Player", true, 0.48f, 0.62f, 1.0f),
        AppElementConfig(PLAYER_DOWNLOAD, "Player Download Button", "Player", true, 0.72f, 0.62f, 1.0f),
        AppElementConfig(PLAYER_SUBSCRIBE, "Player Subscribe Button", "Player", true, 0.84f, 0.54f, 1.0f),
        AppElementConfig(PLAYER_SKIP_BACK, "Skip Backward 10s", "Player", true, 0.25f, 0.28f, 1.0f),
        AppElementConfig(PLAYER_SKIP_FWD, "Skip Forward 10s", "Player", true, 0.75f, 0.28f, 1.0f),
        AppElementConfig(PLAYER_MORE, "Player More Menu", "Player", true, 0.92f, 0.62f, 1.0f, isProtected = true),

        // Profile
        AppElementConfig(PROFILE_AVATAR, "Profile Header Avatar", "Profile", true, 0.12f, 0.12f, 1.0f),
        AppElementConfig(PROFILE_SETTINGS, "Profile Settings Icon", "Profile", true, 0.92f, 0.04f, 1.0f, isProtected = true),
        AppElementConfig(PROFILE_EDIT, "Edit Profile Button", "Profile", true, 0.42f, 0.22f, 1.0f),
        AppElementConfig(PROFILE_DOWNLOADS, "Downloads Banner", "Profile", true, 0.50f, 0.38f, 1.0f),

        // Subscriptions
        AppElementConfig("sub_avatar", "Channel Header Avatar", "Subscriptions", true, 0.16f, 0.08f, 1.0f),
        AppElementConfig("sub_filter", "Subscriptions Filter Chips", "Subscriptions", true, 0.50f, 0.16f, 1.0f),
        AppElementConfig("sub_subscribe", "Subscriptions Bell / Toggle", "Subscriptions", true, 0.88f, 0.08f, 1.0f),

        // Search
        AppElementConfig("search_mic", "Voice Search Mic", "Search", true, 0.90f, 0.04f, 1.0f),
        AppElementConfig("search_clear", "Clear Search Button", "Search", true, 0.80f, 0.04f, 1.0f),
        AppElementConfig("search_fab_mic", "Floating Mic Button", "Search", true, 0.88f, 0.88f, 1.0f),

        // Notifications
        AppElementConfig("notif_mark_read", "Mark All Read Icon", "Notifications", true, 0.85f, 0.04f, 1.0f),
        AppElementConfig("notif_filter", "Notifications Filter", "Notifications", true, 0.40f, 0.10f, 1.0f),

        // Navigation Dock
        AppElementConfig(NAV_HOME, "Dock Home Tab", "Navigation", true, 0.10f, 0.94f, 1.0f),
        AppElementConfig(NAV_SHORTS, "Dock Shorts Tab", "Navigation", true, 0.30f, 0.94f, 1.0f),
        AppElementConfig(NAV_PLUS, "Dock Plus (+) Button", "Navigation", true, 0.50f, 0.94f, 1.0f),
        AppElementConfig(NAV_SUBSCRIPTIONS, "Dock Subscriptions Tab", "Navigation", true, 0.70f, 0.94f, 1.0f),
        AppElementConfig(NAV_YOU, "Dock You / Profile Tab", "Navigation", true, 0.90f, 0.94f, 1.0f)
    )
}
