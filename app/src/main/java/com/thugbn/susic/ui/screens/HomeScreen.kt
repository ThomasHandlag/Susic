
package com.thugbn.susic.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.thugbn.susic.navigation.Screen
import com.thugbn.susic.ui.components.SongItem
import com.thugbn.susic.ui.viewmodel.LibraryViewModel
import com.thugbn.susic.ui.viewmodel.MusicPlayerViewModel

/**
 * Home screen showing overview of library
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    musicPlayerViewModel: MusicPlayerViewModel,
    libraryViewModel: LibraryViewModel
) {
    val recentlyPlayed by libraryViewModel.recentlyPlayedSongs.collectAsState()
    val mostPlayed by libraryViewModel.mostPlayedSongs.collectAsState()
    val favorites by libraryViewModel.favoriteSongs.collectAsState()
    val isScanning by libraryViewModel.isScanning.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Susic") },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Search.route) }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { libraryViewModel.scanDeviceForMusic() }) {
                        if (isScanning) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Filled.Refresh, contentDescription = "Scan library")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Recently Played Section
            if (recentlyPlayed.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Recently Played",
                        onSeeAllClick = { navController.navigate(Screen.Library.route) }
                    )
                }
                items(recentlyPlayed.take(5)) { song ->
                    SongItem(
                        song = song,
                        onClick = {
                            musicPlayerViewModel.playSongs(recentlyPlayed, recentlyPlayed.indexOf(song))
                        },
                        onFavoriteClick = {
                            libraryViewModel.toggleFavorite(song)
                        },
                        onMoreClick = { /* Show bottom sheet */ }
                    )
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }

            // Favorites Section
            if (favorites.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Favorites",
                        onSeeAllClick = { navController.navigate(Screen.Library.route) }
                    )
                }
                items(favorites.take(5)) { song ->
                    SongItem(
                        song = song,
                        onClick = {
                            musicPlayerViewModel.playSongs(favorites, favorites.indexOf(song))
                        },
                        onFavoriteClick = {
                            libraryViewModel.toggleFavorite(song)
                        },
                        onMoreClick = { /* Show bottom sheet */ }
                    )
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }

            // Most Played Section
            if (mostPlayed.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Most Played",
                        onSeeAllClick = { navController.navigate(Screen.Library.route) }
                    )
                }
                items(mostPlayed.take(5)) { song ->
                    SongItem(
                        song = song,
                        onClick = {
                            musicPlayerViewModel.playSongs(mostPlayed, mostPlayed.indexOf(song))
                        },
                        onFavoriteClick = {
                            libraryViewModel.toggleFavorite(song)
                        },
                        onMoreClick = { /* Show bottom sheet */ }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall
        )
        TextButton(onClick = onSeeAllClick) {
            Text("See All")
        }
    }
}
