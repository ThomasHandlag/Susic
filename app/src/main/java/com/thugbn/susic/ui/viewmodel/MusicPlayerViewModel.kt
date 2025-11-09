
package com.thugbn.susic.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thugbn.susic.data.local.MusicDatabase
import com.thugbn.susic.data.model.PlaybackState
import com.thugbn.susic.data.model.RepeatMode
import com.thugbn.susic.data.model.Song
import com.thugbn.susic.data.preferences.UserPreferencesRepository
import com.thugbn.susic.data.repository.SongRepository
import com.thugbn.susic.service.MusicPlayerController
import com.thugbn.susic.service.SleepTimerManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for managing music playback and player UI state
 */
class MusicPlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MusicDatabase.getDatabase(application)
    private val songRepository = SongRepository(database.songDao())
    private val preferencesRepository = UserPreferencesRepository(application)

    val playerController = MusicPlayerController(application)
    private val sleepTimerManager = SleepTimerManager(application)

    val playbackState: StateFlow<PlaybackState> = playerController.playbackState
    val currentQueue: StateFlow<List<Song>> = playerController.currentQueue
    val currentIndex: StateFlow<Int> = playerController.currentIndex

    private val _sleepTimerActive = MutableStateFlow(false)
    val sleepTimerActive: StateFlow<Boolean> = _sleepTimerActive.asStateFlow()

    private val _sleepTimerDuration = MutableStateFlow(0)
    val sleepTimerDuration: StateFlow<Int> = _sleepTimerDuration.asStateFlow()

    val visualizerType: StateFlow<Int> = preferencesRepository.visualizerType
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    init {
        observePreferences()
        checkSleepTimerStatus()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            preferencesRepository.shuffleEnabled.collect { enabled ->
                playerController.setShuffleEnabled(enabled)
            }
        }

        viewModelScope.launch {
            preferencesRepository.repeatMode.collect { mode ->
                playerController.setRepeatMode(mode)
            }
        }

        viewModelScope.launch {
            preferencesRepository.playbackSpeed.collect { speed ->
                playerController.setPlaybackSpeed(speed)
            }
        }
    }

    private fun checkSleepTimerStatus() {
        _sleepTimerActive.value = sleepTimerManager.isSleepTimerActive()
    }

    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        playerController.playSongs(songs, startIndex)

        // Update play count for the first song
        viewModelScope.launch {
            songs.getOrNull(startIndex)?.let { song ->
                songRepository.incrementPlayCount(song.id)
            }
        }
    }

    fun playPause() {
        playerController.playPause()
    }

    fun seekToNext() {
        playerController.seekToNext()

        // Update play count
        viewModelScope.launch {
            val nextSong = currentQueue.value.getOrNull(currentIndex.value + 1)
            nextSong?.let {
                songRepository.incrementPlayCount(it.id)
            }
        }
    }

    fun seekToPrevious() {
        playerController.seekToPrevious()
    }

    fun seekTo(position: Long) {
        playerController.seekTo(position)
    }

    fun toggleShuffle() {
        viewModelScope.launch {
            val currentShuffle = playbackState.value.shuffleEnabled
            preferencesRepository.updateShuffleEnabled(!currentShuffle)
        }
    }

    fun cycleRepeatMode() {
        viewModelScope.launch {
            val currentMode = playbackState.value.repeatMode
            val nextMode = when (currentMode) {
                RepeatMode.OFF -> RepeatMode.ALL
                RepeatMode.ALL -> RepeatMode.ONE
                RepeatMode.ONE -> RepeatMode.OFF
            }
            preferencesRepository.updateRepeatMode(nextMode)
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        viewModelScope.launch {
            preferencesRepository.updatePlaybackSpeed(speed)
        }
    }

    fun toggleFavorite(songId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            songRepository.toggleFavorite(songId, !isFavorite)
        }
    }

    fun setSleepTimer(durationMinutes: Int) {
        sleepTimerManager.scheduleSleepTimer(durationMinutes)
        _sleepTimerActive.value = true
        _sleepTimerDuration.value = durationMinutes

        viewModelScope.launch {
            preferencesRepository.updateSleepTimerDuration(durationMinutes.toLong())
        }
    }

    fun cancelSleepTimer() {
        sleepTimerManager.cancelSleepTimer()
        _sleepTimerActive.value = false
        _sleepTimerDuration.value = 0

        viewModelScope.launch {
            preferencesRepository.updateSleepTimerDuration(0L)
        }
    }

    fun setVisualizerType(typeOrdinal: Int) {
        viewModelScope.launch {
            preferencesRepository.updateVisualizerType(typeOrdinal)
        }
    }

    override fun onCleared() {
        super.onCleared()
        playerController.release()
    }
}