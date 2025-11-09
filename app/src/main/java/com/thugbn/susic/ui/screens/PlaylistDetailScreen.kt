package com.thugbn.susic.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.thugbn.susic.ui.components.SongItem
import com.thugbn.susic.ui.viewmodel.MusicPlayerViewModel
import com.thugbn.susic.ui.viewmodel.PlaylistViewModel

/**
 * Playlist detail screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    navController: NavController,
    playlistViewModel: PlaylistViewModel,
    musicPlayerViewModel: MusicPlayerViewModel
) {
    LaunchedEffect(playlistId) {
        playlistViewModel.selectPlaylist(playlistId)
    }

    val playlistWithSongs by playlistViewModel.selectedPlaylist.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(playlistWithSongs?.playlist?.name ?: "Playlist") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Edit playlist */ }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                }
            )
        },
        floatingActionButton = {
            if (playlistWithSongs?.songs?.isNotEmpty() == true) {
                FloatingActionButton(
                    onClick = {
                        playlistWithSongs?.songs?.let { songs ->
                            musicPlayerViewModel.playSongs(songs)
                        }
                    }
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Play all")
                }
            }
        }
    ) { paddingValues ->
        playlistWithSongs?.let { playlist ->
            if (playlist.songs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No songs in this playlist")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    item {
                        // Playlist Header
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = playlist.playlist.name,
                                style = MaterialTheme.typography.headlineMedium
                            )
                            playlist.playlist.description?.let { desc ->
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${playlist.songs.size} songs",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    items(playlist.songs) { song ->
                        SongItem(
                            song = song,
                            onClick = {
                                musicPlayerViewModel.playSongs(
                                    playlist.songs,
                                    playlist.songs.indexOf(song)
                                )
                            },
                            onFavoriteClick = {
                                // Toggle favorite in song repository
                            },
                            onMoreClick = {
                                // Show options to remove from playlist
                            }
                        )
                    }
                }
            }
        }
    }
}


