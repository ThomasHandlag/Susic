package com.thugbn.susic.navigation

/**
 * Navigation routes for the app
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Library : Screen("library")
    object Playlists : Screen("playlists")
    object PlaylistDetail : Screen("playlist/{playlistId}") {
        fun createRoute(playlistId: Long) = "playlist/$playlistId"
    }
    object NowPlaying : Screen("now_playing")
    object Settings : Screen("settings")
    object Search : Screen("search")
    object Equalizer : Screen("equalizer")
}

