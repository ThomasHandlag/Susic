package com.thugbn.susic.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.thugbn.susic.ui.viewmodel.MusicPlayerViewModel

/**
 * Settings screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    musicPlayerViewModel: MusicPlayerViewModel
) {
    val playbackState by musicPlayerViewModel.playbackState.collectAsState()
    var showSpeedDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                Text(
                    text = "Playback",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("Playback Speed") },
                    supportingContent = { Text("${playbackState.playbackSpeed}x") },
                    leadingContent = {
                        Icon(Icons.Filled.Speed, contentDescription = null)
                    },
                    modifier = Modifier.clickable { showSpeedDialog = true }
                )
            }

            item {
                Divider()
            }

            item {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("Version") },
                    supportingContent = { Text("1.0.0") },
                    leadingContent = {
                        Icon(Icons.Filled.Info, contentDescription = null)
                    }
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("Open Source Licenses") },
                    leadingContent = {
                        Icon(Icons.Filled.Description, contentDescription = null)
                    },
                    modifier = Modifier.clickable { /* Open licenses */ }
                )
            }
        }
    }

    if (showSpeedDialog) {
        PlaybackSpeedDialog(
            currentSpeed = playbackState.playbackSpeed,
            onDismiss = { showSpeedDialog = false },
            onSpeedSelected = { speed ->
                musicPlayerViewModel.setPlaybackSpeed(speed)
                showSpeedDialog = false
            }
        )
    }
}

@Composable
fun PlaybackSpeedDialog(
    currentSpeed: Float,
    onDismiss: () -> Unit,
    onSpeedSelected: (Float) -> Unit
) {
    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Playback Speed") },
        text = {
            Column {
                speeds.forEach { speed ->
                    TextButton(
                        onClick = { onSpeedSelected(speed) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${speed}x")
                            if (speed == currentSpeed) {
                                Icon(Icons.Filled.Check, contentDescription = "Selected")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

