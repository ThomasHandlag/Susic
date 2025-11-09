package com.thugbn.susic.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.thugbn.susic.service.EqualizerBand
import com.thugbn.susic.ui.components.AudioVisualizer
import com.thugbn.susic.ui.components.VisualizerType
import com.thugbn.susic.ui.viewmodel.MusicPlayerViewModel

/**
 * Equalizer and Audio Visualizer screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    navController: NavController,
    musicPlayerViewModel: MusicPlayerViewModel
) {
    val audioEffectsManager = musicPlayerViewModel.playerController.audioEffectsManager
    val equalizerBands by audioEffectsManager?.equalizerBands?.collectAsState()
        ?: remember { mutableStateOf(emptyList()) }
    val waveform by audioEffectsManager?.waveform?.collectAsState()
        ?: remember { mutableStateOf(ByteArray(0)) }
    val fft by audioEffectsManager?.fft?.collectAsState()
        ?: remember { mutableStateOf(ByteArray(0)) }
    val isVisualizerEnabled by audioEffectsManager?.isVisualizerEnabled?.collectAsState()
        ?: remember { mutableStateOf(false) }

    var selectedVisualizerType by remember { mutableStateOf(VisualizerType.BAR) }
    var showPresetsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Equalizer & Visualizer") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showPresetsDialog = true }) {
                        Icon(Icons.Filled.LibraryMusic, contentDescription = "Presets")
                    }
                    IconButton(onClick = { audioEffectsManager?.resetEqualizer() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Reset")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Visualizer Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Audio Visualizer",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Switch(
                                checked = isVisualizerEnabled,
                                onCheckedChange = {
                                    audioEffectsManager?.setVisualizerEnabled(it)
                                }
                            )
                        }

                        if (isVisualizerEnabled) {
                            Spacer(modifier = Modifier.height(16.dp))

                            // Visualizer Display
                            AudioVisualizer(
                                waveform = waveform,
                                fft = fft,
                                visualizerType = selectedVisualizerType,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                color = MaterialTheme.colorScheme.primary,
                                accentColor = MaterialTheme.colorScheme.secondary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Visualizer Type Selector
                            Text(
                                text = "Visualization Style",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                VisualizerType.entries.forEach { type ->
                                    FilterChip(
                                        selected = selectedVisualizerType == type,
                                        onClick = { selectedVisualizerType = type },
                                        label = { Text(type.name) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Equalizer Section
            item {
                Text(
                    text = "Equalizer",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            items(equalizerBands) { band ->
                EqualizerBandControl(
                    band = band,
                    onLevelChange = { level ->
                        audioEffectsManager?.setBandLevel(band.index, level)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Presets Dialog
    if (showPresetsDialog) {
        val presets = audioEffectsManager?.getPresets() ?: emptyList()

        AlertDialog(
            onDismissRequest = { showPresetsDialog = false },
            title = { Text("Equalizer Presets") },
            text = {
                LazyColumn {
                    items(presets.size) { index ->
                        TextButton(
                            onClick = {
                                audioEffectsManager?.usePreset(index)
                                showPresetsDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(presets[index])
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPresetsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

/**
 * Individual equalizer band control
 */
@Composable
private fun EqualizerBandControl(
    band: EqualizerBand,
    onLevelChange: (Int) -> Unit
) {
    var sliderValue by remember(band.currentLevel) {
        mutableFloatStateOf(band.currentLevel.toFloat())
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatFrequency(band.frequency),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${band.currentLevel / 100} dB",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                onValueChangeFinished = { onLevelChange(sliderValue.toInt()) },
                valueRange = band.minLevel.toFloat()..band.maxLevel.toFloat(),
                steps = 30
            )
        }
    }
}

/**
 * Format frequency for display
 */
private fun formatFrequency(frequency: Int): String {
    return when {
        frequency < 1000 -> "${frequency} Hz"
        else -> "${"%.1f".format(frequency / 1000f)} kHz"
    }
}

