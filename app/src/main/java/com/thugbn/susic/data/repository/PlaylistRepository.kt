package com.thugbn.susic.data.repository

import com.thugbn.susic.data.local.PlaylistDao
import com.thugbn.susic.data.model.Playlist
import com.thugbn.susic.data.model.PlaylistSongCrossRef
import com.thugbn.susic.data.model.PlaylistWithSongs
import kotlinx.coroutines.flow.Flow

/**
 * Repository for Playlist data operations
 */
class PlaylistRepository(private val playlistDao: PlaylistDao) {

    fun getAllPlaylists(): Flow<List<Playlist>> = playlistDao.getAllPlaylists()

    fun getAllPlaylistsWithSongs(): Flow<List<PlaylistWithSongs>> =
        playlistDao.getAllPlaylistsWithSongs()

    fun getPlaylistWithSongs(playlistId: Long): Flow<PlaylistWithSongs?> =
        playlistDao.getPlaylistWithSongs(playlistId)

    suspend fun getPlaylistById(playlistId: Long): Playlist? =
        playlistDao.getPlaylistById(playlistId)

    suspend fun createPlaylist(playlist: Playlist): Long =
        playlistDao.insertPlaylist(playlist)

    suspend fun updatePlaylist(playlist: Playlist) = playlistDao.updatePlaylist(playlist)

    suspend fun deletePlaylist(playlist: Playlist) = playlistDao.deletePlaylist(playlist)

    suspend fun addSongToPlaylist(playlistId: Long, songId: String) {
        val maxPosition = playlistDao.getMaxPosition(playlistId) ?: -1
        val crossRef = PlaylistSongCrossRef(
            playlistId = playlistId,
            songId = songId,
            position = maxPosition + 1
        )
        playlistDao.insertPlaylistSong(crossRef)
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: String) =
        playlistDao.removePlaylistSong(playlistId, songId)

    suspend fun clearPlaylist(playlistId: Long) = playlistDao.clearPlaylist(playlistId)
}

