package com.thugbn.susic.service

import android.content.ComponentName
import android.content.Context
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.thugbn.susic.data.model.PlaybackState
import com.thugbn.susic.data.model.RepeatMode
import com.thugbn.susic.data.model.Song
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Controller for managing music playback
 * Acts as a bridge between UI and MediaSessionService
 */
class MusicPlayerController(private val context: Context) {

    private var mediaController: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentQueue = MutableStateFlow<List<Song>>(emptyList())
    val currentQueue: StateFlow<List<Song>> = _currentQueue.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var positionUpdateJob: Job? = null

    var audioEffectsManager: AudioEffectsManager? = null
        private set

    init {
        initializeController()
    }

    private fun initializeController() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MusicPlaybackService::class.java)
        )

        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            mediaController = controllerFuture?.get()
            setupPlayerListener()
            initializeAudioEffects()
        }, MoreExecutors.directExecutor())
    }

    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatePlaybackState()
                if (isPlaying) {
                    startPositionUpdates()
                } else {
                    stopPositionUpdates()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updatePlaybackState()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                updatePlaybackState()
            }
        })

        // Start position updates if already playing
        if (mediaController?.isPlaying == true) {
            startPositionUpdates()
        }
    }

    private fun startPositionUpdates() {
        stopPositionUpdates()
        positionUpdateJob = scope.launch {
            while (isActive) {
                updatePlaybackState()
                delay(500) // Update every 500ms for smooth UI
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }

    @OptIn(UnstableApi::class)
    private fun initializeAudioEffects() {
        scope.launch {
            try {
                // Request audio session ID from the service using custom command
                val command = androidx.media3.session.SessionCommand(
                    MusicPlaybackService.COMMAND_GET_AUDIO_SESSION_ID,
                    android.os.Bundle.EMPTY
                )

                val resultFuture = mediaController?.sendCustomCommand(command, android.os.Bundle.EMPTY)

                resultFuture?.let { future ->
                    // Wait for the result in a background thread
                    withContext(Dispatchers.IO) {
                        try {
                            val result = future.get()
                            val sessionId = result.extras.getInt(
                                MusicPlaybackService.KEY_AUDIO_SESSION_ID,
                                0
                            )

                            if (sessionId != 0) {
                                withContext(Dispatchers.Main) {
                                    audioEffectsManager = AudioEffectsManager(sessionId)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updatePlaybackState() {
        mediaController?.let { controller ->
            val currentSong = _currentQueue.value.getOrNull(controller.currentMediaItemIndex)

            _playbackState.value = PlaybackState(
                currentSong = currentSong,
                isPlaying = controller.isPlaying,
                currentPosition = controller.currentPosition,
                duration = controller.duration,
                shuffleEnabled = controller.shuffleModeEnabled,
                repeatMode = when (controller.repeatMode) {
                    Player.REPEAT_MODE_OFF -> RepeatMode.OFF
                    Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                    Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                    else -> RepeatMode.OFF
                }
            )

            _currentIndex.value = controller.currentMediaItemIndex
        }
    }

    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        _currentQueue.value = songs
        val mediaItems = songs.map { song -> song.toMediaItem() }

        mediaController?.apply {
            setMediaItems(mediaItems, startIndex, 0)
            prepare()
            play()
        }
    }

    fun play() {
        mediaController?.play()
    }

    fun pause() {
        mediaController?.pause()
    }

    fun playPause() {
        mediaController?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.play()
            }
        }
    }

    fun seekToNext() {
        mediaController?.seekToNext()
    }

    fun seekToPrevious() {
        mediaController?.seekToPrevious()
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }

    fun setShuffleEnabled(enabled: Boolean) {
        mediaController?.shuffleModeEnabled = enabled
        updatePlaybackState()
    }

    fun setRepeatMode(mode: RepeatMode) {
        val repeatMode = when (mode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
        }
        mediaController?.repeatMode = repeatMode
        updatePlaybackState()
    }

    fun setPlaybackSpeed(speed: Float) {
        mediaController?.setPlaybackSpeed(speed)
    }

    fun stop() {
        mediaController?.stop()
        mediaController?.clearMediaItems()
    }

    fun release() {
        stopPositionUpdates()
        scope.cancel()
        audioEffectsManager?.release()
        audioEffectsManager = null
        mediaController?.release()
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
    }

    private fun Song.toMediaItem(): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)
            .setArtworkUri(albumArtUri?.toUri())
            .build()

        return MediaItem.Builder()
            .setUri(filePath)
            .setMediaId(id)
            .setMediaMetadata(metadata)
            .build()
    }
}

