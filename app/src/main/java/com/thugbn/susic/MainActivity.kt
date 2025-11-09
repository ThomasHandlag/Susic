package com.thugbn.susic

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.thugbn.susic.navigation.NavigationGraph
import com.thugbn.susic.navigation.Screen
import com.thugbn.susic.ui.components.MiniPlayerBar
import com.thugbn.susic.ui.components.SuScaffold
import com.thugbn.susic.ui.theme.SusicTheme
import com.thugbn.susic.ui.viewmodel.LibraryViewModel
import com.thugbn.susic.ui.viewmodel.MusicPlayerViewModel
import com.thugbn.susic.ui.viewmodel.PlaylistViewModel

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { isGranted ->
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.hide()
        // Request permissions for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.RECORD_AUDIO
                )
            )
        }

        setContent {
            SusicTheme {
                SusicApp()
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class
)
@Composable
fun SusicApp() {
    val navController = rememberNavController()
    val musicPlayerViewModel: MusicPlayerViewModel = viewModel()
    val libraryViewModel: LibraryViewModel = viewModel()
    val playlistViewModel: PlaylistViewModel = viewModel()

    val playbackState by musicPlayerViewModel.playbackState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Request storage and audio permissions
    val permissionsState = rememberMultiplePermissionsState(
        permissions = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_AUDIO)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            add(Manifest.permission.RECORD_AUDIO)
        }
    )

    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    // Bottom navigation items
    val bottomNavItems = listOf(
        BottomNavItem("Home", Screen.Home.route, Icons.Filled.Home),
        BottomNavItem("Library", Screen.Library.route, Icons.Filled.LibraryMusic),
        BottomNavItem("Playlists", Screen.Playlists.route, Icons.Filled.QueueMusic),
        BottomNavItem("Settings", Screen.Settings.route, Icons.Filled.Settings)
    )

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }
    val showMiniPlayer = playbackState.currentSong != null && currentRoute != Screen.NowPlaying.route

    SuScaffold(
       bottomBar = {
           Column {
               // Mini Player
               if (showMiniPlayer) {
                   MiniPlayerBar(
                       playbackState = playbackState,
                       onPlayPauseClick = { musicPlayerViewModel.playPause() },
                       onNextClick = { musicPlayerViewModel.seekToNext() },
                       onBarClick = { navController.navigate(Screen.NowPlaying.route) }
                   )
               }

               // Bottom Navigation
               if (showBottomBar) {
                   NavigationBar {
                       bottomNavItems.forEach { item ->
                           NavigationBarItem(
                               icon = { Icon(item.icon, contentDescription = item.label) },
                               label = { Text(item.label) },
                               selected = currentRoute == item.route,
                               onClick = {
                                   if (currentRoute != item.route) {
                                       navController.navigate(item.route) {
                                           popUpTo(Screen.Home.route) {
                                               saveState = true
                                           }
                                           launchSingleTop = true
                                           restoreState = true
                                       }
                                   }
                               }
                           )
                       }
                   }
               }
           }
       }
   ) { paddingValues ->
       NavigationGraph(
           navController = navController,
           paddingValues = paddingValues,
           musicPlayerViewModel = musicPlayerViewModel,
           libraryViewModel = libraryViewModel,
           playlistViewModel = playlistViewModel
       )
   }

    // Show permission rationale if needed
    if (!permissionsState.allPermissionsGranted && permissionsState.shouldShowRationale) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Storage Permission Required") },
            text = { Text("This app needs storage permission to access your music files.") },
            confirmButton = {
                TextButton(onClick = { permissionsState.launchMultiplePermissionRequest() }) {
                    Text("Grant Permission")
                }
            }
        )
    }
}

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

