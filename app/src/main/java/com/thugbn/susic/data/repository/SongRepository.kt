package com.thugbn.susic.data.repository

import com.thugbn.susic.data.local.SongDao
import com.thugbn.susic.data.model.Song
import kotlinx.coroutines.flow.Flow

/**
 * Repository for Song data operations
 * Single source of truth for song data
 */
class SongRepository(private val songDao: SongDao) {

    fun getAllSongs(): Flow<List<Song>> = songDao.getAllSongs()

    fun getFavoriteSongs(): Flow<List<Song>> = songDao.getFavoriteSongs()

    fun getMostPlayedSongs(limit: Int = 20): Flow<List<Song>> =
        songDao.getMostPlayedSongs(limit)

    fun getRecentlyPlayedSongs(limit: Int = 20): Flow<List<Song>> =
        songDao.getRecentlyPlayedSongs(limit)

    fun searchSongs(query: String): Flow<List<Song>> = songDao.searchSongs(query)

    suspend fun getSongById(songId: String): Song? = songDao.getSongById(songId)

    suspend fun insertSong(song: Song) = songDao.insertSong(song)

    suspend fun insertSongs(songs: List<Song>) = songDao.insertSongs(songs)

    suspend fun updateSong(song: Song) = songDao.updateSong(song)

    suspend fun deleteSong(song: Song) = songDao.deleteSong(song)

    suspend fun deleteAllSongs() = songDao.deleteAllSongs()

    suspend fun toggleFavorite(songId: String, isFavorite: Boolean) =
        songDao.updateFavoriteStatus(songId, isFavorite)

    suspend fun incrementPlayCount(songId: String) =
        songDao.incrementPlayCount(songId)
}