package com.thugbn.susic.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.thugbn.susic.ui.components.SongItem
import com.thugbn.susic.ui.viewmodel.LibraryViewModel
import com.thugbn.susic.ui.viewmodel.MusicPlayerViewModel

/**
 * Library screen showing all songs
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    navController: NavController,
    libraryViewModel: LibraryViewModel,
    musicPlayerViewModel: MusicPlayerViewModel
) {
    val allSongs by libraryViewModel.allSongs.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("All Songs", "Favorites", "Most Played", "Recently Played")

    val displaySongs = when (selectedTab) {
        0 -> allSongs
        1 -> {
            val favorites by libraryViewModel.favoriteSongs.collectAsState()
            favorites
        }
        2 -> {
            val mostPlayed by libraryViewModel.mostPlayedSongs.collectAsState()
            mostPlayed
        }
        3 -> {
            val recentlyPlayed by libraryViewModel.recentlyPlayedSongs.collectAsState()
            recentlyPlayed
        }
        else -> allSongs
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Library") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            ScrollableTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            // Songs List
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(displaySongs) { song ->
                    SongItem(
                        song = song,
                        onClick = {
                            musicPlayerViewModel.playSongs(displaySongs, displaySongs.indexOf(song))
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

