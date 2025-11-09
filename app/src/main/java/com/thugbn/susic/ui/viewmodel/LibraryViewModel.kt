package com.thugbn.susic.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thugbn.susic.data.local.MusicDatabase
import com.thugbn.susic.data.model.Song
import com.thugbn.susic.data.repository.MediaScanRepository
import com.thugbn.susic.data.repository.SongRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for managing song library
 */
class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MusicDatabase.getDatabase(application)
    private val songRepository = SongRepository(database.songDao())
    private val mediaScanRepository = MediaScanRepository(application.contentResolver)

    val allSongs: StateFlow<List<Song>> = songRepository.getAllSongs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoriteSongs: StateFlow<List<Song>> = songRepository.getFavoriteSongs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val mostPlayedSongs: StateFlow<List<Song>> = songRepository.getMostPlayedSongs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentlyPlayedSongs: StateFlow<List<Song>> = songRepository.getRecentlyPlayedSongs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<Song>> = searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(emptyList())
            } else {
                songRepository.searchSongs(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun scanDeviceForMusic() {
        viewModelScope.launch {
            _isScanning.value = true
            try {
                val songs = mediaScanRepository.scanDeviceForMusic()
                songRepository.insertSongs(songs)
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            songRepository.toggleFavorite(song.id, !song.isFavorite)
        }
    }

    fun deleteSong(song: Song) {
        viewModelScope.launch {
            songRepository.deleteSong(song)
        }
    }
}

