package com.thugbn.susic.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.thugbn.susic.ui.screens.*
import com.thugbn.susic.ui.viewmodel.LibraryViewModel
import com.thugbn.susic.ui.viewmodel.MusicPlayerViewModel
import com.thugbn.susic.ui.viewmodel.PlaylistViewModel

/**
 * Navigation graph for the app
 */
@Composable
fun NavigationGraph(
    navController: NavHostController,
    paddingValues: PaddingValues,
    musicPlayerViewModel: MusicPlayerViewModel = viewModel(),
    libraryViewModel: LibraryViewModel = viewModel(),
    playlistViewModel: PlaylistViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                musicPlayerViewModel = musicPlayerViewModel,
                libraryViewModel = libraryViewModel
            )
        }

        composable(Screen.Library.route) {
            LibraryScreen(
                navController = navController,
                libraryViewModel = libraryViewModel,
                musicPlayerViewModel = musicPlayerViewModel
            )
        }

        composable(Screen.Playlists.route) {
            PlaylistsScreen(
                navController = navController,
                playlistViewModel = playlistViewModel
            )
        }

        composable(
            route = Screen.PlaylistDetail.route,
            arguments = listOf(
                navArgument("playlistId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: return@composable
            PlaylistDetailScreen(
                playlistId = playlistId,
                navController = navController,
                playlistViewModel = playlistViewModel,
                musicPlayerViewModel = musicPlayerViewModel
            )
        }

        composable(Screen.NowPlaying.route) {
            NowPlayingScreen(
                navController = navController,
                musicPlayerViewModel = musicPlayerViewModel
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController,
                musicPlayerViewModel = musicPlayerViewModel
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                navController = navController,
                libraryViewModel = libraryViewModel,
                musicPlayerViewModel = musicPlayerViewModel
            )
        }

        composable(Screen.Equalizer.route) {
            EqualizerScreen(
                navController = navController,
                musicPlayerViewModel = musicPlayerViewModel
            )
        }
    }
}

