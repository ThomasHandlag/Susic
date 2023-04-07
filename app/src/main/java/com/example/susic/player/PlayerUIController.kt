package com.example.susic.player

import com.example.susic.data.Track

interface PlayerUIController {
    fun onAppearanceChanged(isThemeChanged: Boolean)
    fun onOpenPlayingArtistAlbum()
}

interface MediaControllerInterface {
    fun onSongSelected(song: Track?, songs: List<Track>?, songLaunchedBy: String)
    fun onSongsShuffled(songs: List<Track>?, songLaunchedBy: String)
    fun onAddToQueue(song: Track?)
    // first: force play, second: restore song
    fun onAddAlbumToQueue(songs: List<Track>?, forcePlay: Pair<Boolean, Track?>)
    fun onUpdatePlayingAlbumSongs(songs: List<Track>?)
    fun onPlaybackSpeedToggled()
    fun onHandleCoverOptionsUpdate()
    fun onUpdatePositionFromNP(position: Int)
}