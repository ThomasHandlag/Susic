package com.thugbn.susic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.thugbn.susic.navigation.Screen
import com.thugbn.susic.ui.components.AudioVisualizer
import com.thugbn.susic.ui.components.PlayerControls
import com.thugbn.susic.ui.components.SongItem
import com.thugbn.susic.ui.components.VisualizerType
import com.thugbn.susic.ui.viewmodel.MusicPlayerViewModel

/**
 * Now Playing screen with full player controls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    navController: NavController, musicPlayerViewModel: MusicPlayerViewModel
) {
    val playbackState by musicPlayerViewModel.playbackState.collectAsState()
    val currentQueue by musicPlayerViewModel.currentQueue.collectAsState()
    val currentIndex by musicPlayerViewModel.currentIndex.collectAsState()
    val currentSong = playbackState.currentSong

    val pagerState = rememberPagerState(pageCount = { 2 })

    val audioEffectsManager = musicPlayerViewModel.playerController.audioEffectsManager
    val waveform by audioEffectsManager?.waveform?.collectAsState() ?: remember {
        mutableStateOf(
            ByteArray(0)
        )
    }
    val fft by audioEffectsManager?.fft?.collectAsState()
        ?: remember { mutableStateOf(ByteArray(0)) }
    val isVisualizerEnabled by audioEffectsManager?.isVisualizerEnabled?.collectAsState()
        ?: remember { mutableStateOf(false) }

    var showQueue by remember { mutableStateOf(false) }
    var showSleepTimer by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Album Art, 1 = Visualizer

    // Load visualizer type from preferences
    val savedVisualizerTypeOrdinal by musicPlayerViewModel.visualizerType.collectAsState()
    var visualizerType by remember { mutableStateOf(VisualizerType.entries[0]) }

    // Update visualizer type when preference changes
    LaunchedEffect(savedVisualizerTypeOrdinal) {
        visualizerType =
            VisualizerType.entries.getOrElse(savedVisualizerTypeOrdinal) { VisualizerType.BAR }
    }

    // Enable visualizer when playing
    LaunchedEffect(playbackState.isPlaying, audioEffectsManager) {
        if (playbackState.isPlaying && audioEffectsManager != null && !isVisualizerEnabled) {
            audioEffectsManager.setVisualizerEnabled(true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Close")
                }
            }, actions = {
                IconButton(onClick = { showSleepTimer = true }) {
                    Icon(Icons.Filled.Timer, contentDescription = "Sleep timer")
                }
                IconButton(onClick = { navController.navigate(Screen.Equalizer.route) }) {
                    Icon(Icons.Filled.Equalizer, contentDescription = "Equalizer")
                }
                IconButton(onClick = { showQueue = !showQueue }) {
                    Icon(Icons.Filled.QueueMusic, contentDescription = "Queue")
                }
            })
        }) { paddingValues ->
        if (showQueue) {
            // Show Queue
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                item {
                    Text(
                        text = "Queue",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                items(currentQueue) { song ->
                    val isCurrentSong = currentQueue.indexOf(song) == currentIndex
                    SongItem(
                        song = song, onClick = {
                        musicPlayerViewModel.playSongs(currentQueue, currentQueue.indexOf(song))
                    }, onFavoriteClick = {
                        musicPlayerViewModel.toggleFavorite(song.id, song.isFavorite)
                    }, onMoreClick = { }, modifier = Modifier.background(
                        if (isCurrentSong) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                    )
                    )
                }
            }
        } else {
            // Show Now Playing UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(1f))

                // Tab Row for Album Art / Visualizer
                HorizontalPager(
                    state = pagerState
                ) { page ->
                    if (page == 0) {
                        AsyncImage(
                            model = currentSong?.albumArtUri,
                            contentDescription = "Album art",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(randomGradient()),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AudioVisualizer(
                                    waveform = waveform,
                                    fft = fft,
                                    visualizerType = visualizerType,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    accentColor = MaterialTheme.colorScheme.secondary
                                )
                                IconButton(
                                    onClick = {
                                        val types = VisualizerType.entries
                                        val currentIndex = types.indexOf(visualizerType)
                                        val newType = types[(currentIndex + 1) % types.size]
                                        visualizerType = newType
                                        musicPlayerViewModel.setVisualizerType(newType.ordinal)
                                    }, modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Autorenew,
                                        contentDescription = "Change visualizer type",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                }
                Spacer(modifier = Modifier.height(32.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentSong?.title ?: "No song playing",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentSong?.artist ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Player Controls
                PlayerControls(
                    playbackState = playbackState,
                    onPlayPauseClick = { musicPlayerViewModel.playPause() },
                    onNextClick = { musicPlayerViewModel.seekToNext() },
                    onPreviousClick = { musicPlayerViewModel.seekToPrevious() },
                    onShuffleClick = { musicPlayerViewModel.toggleShuffle() },
                    onRepeatClick = { musicPlayerViewModel.cycleRepeatMode() },
                    onSeek = { position -> musicPlayerViewModel.seekTo(position) })

                Spacer(modifier = Modifier.weight(1f))
            }

            // Sleep Timer Dialog
            if (showSleepTimer) {
                SleepTimerDialog(onDismiss = { showSleepTimer = false }, onSetTimer = { minutes ->
                    musicPlayerViewModel.setSleepTimer(minutes)
                    showSleepTimer = false
                }, onCancelTimer = {
                    musicPlayerViewModel.cancelSleepTimer()
                    showSleepTimer = false
                })
            }
        }
    }
}

@Composable
fun randomGradient(): Brush {
    val colors = remember {
        listOf(
            Color(0xFF6366F1),
            Color(0xFF8B5CF6),
            Color(0xFFEC4899),
            Color(0xFFF43F5E),
            Color(0xFFF59E0B),
            Color(0xFF10B981)
        ).shuffled().take(2)
    }
    return Brush.linearGradient(colors)
}