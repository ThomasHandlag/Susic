package com.thugbn.susic.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thugbn.susic.data.local.MusicDatabase
import com.thugbn.susic.data.model.Playlist
import com.thugbn.susic.data.model.PlaylistWithSongs
import com.thugbn.susic.data.repository.PlaylistRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for managing playlists
 */
class PlaylistViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MusicDatabase.getDatabase(application)
    private val playlistRepository = PlaylistRepository(database.playlistDao())

    val allPlaylists: StateFlow<List<Playlist>> = playlistRepository.getAllPlaylists()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allPlaylistsWithSongs: StateFlow<List<PlaylistWithSongs>> =
        playlistRepository.getAllPlaylistsWithSongs()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private val _selectedPlaylistId = MutableStateFlow<Long?>(null)
    val selectedPlaylistId: StateFlow<Long?> = _selectedPlaylistId.asStateFlow()

    val selectedPlaylist: StateFlow<PlaylistWithSongs?> = selectedPlaylistId
        .flatMapLatest { playlistId ->
            if (playlistId != null) {
                playlistRepository.getPlaylistWithSongs(playlistId)
            } else {
                flowOf(null)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun selectPlaylist(playlistId: Long?) {
        _selectedPlaylistId.value = playlistId
    }

    fun createPlaylist(name: String, description: String? = null) {
        viewModelScope.launch {
            val playlist = Playlist(
                name = name,
                description = description
            )
            playlistRepository.createPlaylist(playlist)
        }
    }

    fun updatePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            playlistRepository.updatePlaylist(
                playlist.copy(updatedAt = System.currentTimeMillis())
            )
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            playlistRepository.deletePlaylist(playlist)
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: String) {
        viewModelScope.launch {
            playlistRepository.addSongToPlaylist(playlistId, songId)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: String) {
        viewModelScope.launch {
            playlistRepository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun clearPlaylist(playlistId: Long) {
        viewModelScope.launch {
            playlistRepository.clearPlaylist(playlistId)
        }
    }
}

