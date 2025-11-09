package com.thugbn.susic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.thugbn.susic.data.model.PlaybackState
import com.thugbn.susic.data.model.RepeatMode
import com.thugbn.susic.data.model.Song

/**
 * Mini player bar shown at the bottom of the screen
 */
@Composable
fun MiniPlayerBar(
    playbackState: PlaybackState,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onBarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSong = playbackState.currentSong ?: return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onBarClick),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 3.dp
    ) {
        Column {
            // Progress Bar
            LinearProgressIndicator(
                progress = {
                    if (playbackState.duration > 0) {
                        playbackState.currentPosition.toFloat() / playbackState.duration.toFloat()
                    } else {
                        0f
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album Art
                AsyncImage(
                    model = currentSong.albumArtUri,
                    contentDescription = "Album art",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Song Info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = currentSong.title,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currentSong.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Play/Pause Button
                IconButton(onClick = onPlayPauseClick) {
                    Icon(
                        imageVector = if (playbackState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Next Button
                IconButton(onClick = onNextClick) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Next"
                    )
                }
            }
        }
    }
}

/**
 * Full player controls component
 */
@Composable
fun PlayerControls(
    playbackState: PlaybackState,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress Slider
        var sliderPosition by remember { mutableFloatStateOf(0f) }
        var isSliding by remember { mutableStateOf(false) }

        LaunchedEffect(playbackState.currentPosition) {
            if (!isSliding) {
                sliderPosition = playbackState.currentPosition.toFloat()
            }
        }

        Slider(
            value = sliderPosition,
            onValueChange = {
                isSliding = true
                sliderPosition = it
            },
            onValueChangeFinished = {
                isSliding = false
                onSeek(sliderPosition.toLong())
            },
            valueRange = 0f..playbackState.duration.toFloat().coerceAtLeast(1f),
            modifier = Modifier.fillMaxWidth()
        )

        // Time Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDuration(playbackState.currentPosition),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = formatDuration(playbackState.duration),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Main Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle Button
            IconButton(onClick = onShuffleClick) {
                Icon(
                    imageVector = Icons.Filled.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (playbackState.shuffleEnabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            // Previous Button
            IconButton(
                onClick = onPreviousClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.SkipPrevious,
                    contentDescription = "Previous",
                    modifier = Modifier.size(32.dp)
                )
            }

            // Play/Pause Button
            FilledIconButton(
                onClick = onPlayPauseClick,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = if (playbackState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(40.dp)
                )
            }

            // Next Button
            IconButton(
                onClick = onNextClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = "Next",
                    modifier = Modifier.size(32.dp)
                )
            }

            // Repeat Button
            IconButton(onClick = onRepeatClick) {
                Icon(
                    imageVector = when (playbackState.repeatMode) {
                        RepeatMode.OFF -> Icons.Filled.Repeat
                        RepeatMode.ALL -> Icons.Filled.Repeat
                        RepeatMode.ONE -> Icons.Filled.RepeatOne
                    },
                    contentDescription = "Repeat mode: ${playbackState.repeatMode}",
                    tint = if (playbackState.repeatMode != RepeatMode.OFF) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}
/**
 * Format duration in milliseconds to MM:SS format
 */
fun formatDuration(durationMillis: Long): String {
    val totalSeconds = durationMillis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

