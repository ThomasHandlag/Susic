package com.example.susic.player

import android.app.ForegroundServiceStartNotAllowedException
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import com.example.susic.PlayerState
import com.example.susic.SusicConstants
import com.example.susic.data.Track
import com.example.susic.ui.home.PostController
import java.lang.IllegalStateException

class MediaPlayerHolder :
    MediaPlayer.OnErrorListener,
    MediaPlayer.OnCompletionListener,
    MediaPlayer.OnPreparedListener {

    private lateinit var mPlayerService: PlayerService
    var sMediaDuration: Int = 0
    var sReadyToPlay = false
    var currentPostIndex: Int? = null
    var isCurrentTrack = true
    private var _notificationPlayerState = PlayerState.IDLE
    val notificationPlayerState: PlayerState
        get() = _notificationPlayerState

    private var _isNotificationPlayerOn = false
    val isNotificationPlayerOn: Boolean
        get() = _isNotificationPlayerOn

    //handler seekbar task
    private var sTaskHandler: Handler? = null

    //store track info
    private var sMediaMetaDataCompat: MediaMetadataCompat? = null

    //player service call back function
    lateinit var mediaPlayerInterface: MediaPlayerInterface

    lateinit var sPostControllerInterface: PostController

    // Media player
    private lateinit var mediaPlayer: MediaPlayer
    private var mSeekBarPositionUpdateTask: Runnable? = null

    var currentSong: Track? = null

    private var _sPlayerDuration = if (isNotificationPlayerOn) mediaPlayer.currentPosition else 0
    val sPlayerDuration: Int
        get() = _sPlayerDuration

    val playerPosition
        get() = try {
            mediaPlayer.currentPosition
        } catch (throwable: IllegalStateException) {
            throwable.printStackTrace()
            0
        }

    // Media player state/booleans
    private val isMediaPlayer get() = ::mediaPlayer.isInitialized

    val isPlaying get() = isMediaPlayer && (state != PlayerState.PAUSE)

    var state = PlayerState.PAUSE
    private var sMusicNotificationManager: MusicNotificationManager? = null
    fun setMusicService(playerService: PlayerService) {
        sTaskHandler = Handler(Looper.getMainLooper())
        mediaPlayer = MediaPlayer()
        mPlayerService = playerService
        if (sMusicNotificationManager == null) sMusicNotificationManager =
            mPlayerService.sNotificationManager

    }

    fun getMediaMetaDataCompat() = sMediaMetaDataCompat

    private fun startUpdatingCallbackWithPosition() {
        if (mSeekBarPositionUpdateTask == null) {
            updateProgressCallbackTask()
        }
    }

    fun setupNotificationPlayer(url: String) {
        mediaPlayer.reset()
        with(mediaPlayer) {
            setOnPreparedListener(this@MediaPlayerHolder)
            setOnCompletionListener(this@MediaPlayerHolder)
            setOnErrorListener(this@MediaPlayerHolder)
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            setDataSource(url)
            prepare()
            _sPlayerDuration = mediaPlayer.duration
        }
    }

    fun playPlayer() {
        _notificationPlayerState = PlayerState.PLAYING
        _isNotificationPlayerOn = true
        mediaPlayerInterface.onStart()
        sMusicNotificationManager?.updateNotification()
        mediaPlayer.start()
    }

    fun pausePlayer() {
        _notificationPlayerState = PlayerState.PAUSE
        mediaPlayer.pause()
        mediaPlayerInterface.onPause()
    }
    fun resetPlayer() {
        _notificationPlayerState = PlayerState.IDLE
        mediaPlayer.pause()
        mediaPlayer.reset()
    }

    private fun stopUpdatingCallbackWithPosition() {
        mSeekBarPositionUpdateTask = null
    }

    private fun updateProgressCallbackTask() {
        if (isPlaying && ::mediaPlayerInterface.isInitialized) {
            mediaPlayerInterface.onPositionChanged(mediaPlayer.currentPosition)
        }
    }

    fun restoreOrStartSeekBarProgress() {

    }

    fun stopUpdateSeekBar() {

    }

    // Reports media playback position to mPlaybackProgressCallback.

    private fun restartMediaPlayer(song: Track?) {
        mediaPlayer = MediaPlayer()
        with(mediaPlayer) {
            setOnPreparedListener(this@MediaPlayerHolder)
            setOnCompletionListener(this@MediaPlayerHolder)
            setOnErrorListener(this@MediaPlayerHolder)
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
        }
        mediaPlayer.setDataSource(song?.url)
        mediaPlayer.prepare()
        sMediaDuration = mediaPlayer.duration
    }

    val currentPlayerPosition: Int
        get() = when (notificationPlayerState) {
            PlayerState.PLAYING -> mediaPlayer.currentPosition
            PlayerState.PAUSE -> mediaPlayer.currentPosition
            else -> 0
        }

    fun setUpMediaURL(url: String, postIndex: Int) {
        currentPostIndex = postIndex
        with(mediaPlayer) {
            if (currentSong?.url != url) {
                reset()
                setOnPreparedListener(this@MediaPlayerHolder)
                setOnCompletionListener(this@MediaPlayerHolder)
                setOnErrorListener(this@MediaPlayerHolder)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(url)
                currentSong = Track(url = url)
                mediaPlayer.prepare()
                sMediaDuration = mediaPlayer.duration
            }
            sReadyToPlay = true
            restoreOrStartSeekBarProgress()
        }
    }

    fun reset() = when (isMediaPlayer) {
        else -> {
            mediaPlayer.reset()
            true
        }
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        mediaPlayer.release()
        if (!isNotificationPlayerOn) restartMediaPlayer(currentSong)
        return true
    }

    override fun onPrepared(mp: MediaPlayer) {
        prepareAllStuff()
    }

    private fun prepareAllStuff() {

    }

    private fun play() {
        with(mediaPlayer) {
            state = PlayerState.PLAYING
            start()
        }
    }

    private fun pause() {
        with(mediaPlayer) {
            try {
                state = PlayerState.PAUSE
                pause()
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            }
        }
    }

    fun resumeOrPause(): PlayerState = when (state) {
        PlayerState.PLAYING -> {
            pause()
            state
        }

        else -> {
            play()
            state
        }
    }


    fun seekTo(pos: Int) {
        mediaPlayer.seekTo(pos)
    }

    fun release() {
        destroyInstance()
//        mediaPlayer.release()
    }

    companion object {
        @Volatile
        private var INSTANCE: MediaPlayerHolder? = null

        /** Get/Instantiate the single instance of [MediaPlayerHolder]. */
        fun getInstance(): MediaPlayerHolder {
            val currentInstance = INSTANCE

            if (currentInstance != null) return currentInstance

            synchronized(this) {
                val newInstance = MediaPlayerHolder()
                INSTANCE = newInstance
                return newInstance
            }
        }
    }

    fun destroyInstance() {
        INSTANCE = null
    }

    override fun onCompletion(player: MediaPlayer?) {
        if (::sPostControllerInterface.isInitialized) sPostControllerInterface.onComplete()
        _notificationPlayerState = PlayerState.IDLE
        mediaPlayer.reset()
        mediaPlayerInterface.onComplete()
    }

    private var sNotificationOngoing = false
    private fun startForeground() {
        if (!sNotificationOngoing) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                sNotificationOngoing = try {
                    sMusicNotificationManager?.createNotification { notification ->
                        mPlayerService.startForeground(SusicConstants.NOF_ID, notification)
                    }
                    true
                } catch (fsNotAllowed: ForegroundServiceStartNotAllowedException) {
                    synchronized(pause()) {

                    }
                    fsNotAllowed.printStackTrace()
                    false
                }
            } else {
                sMusicNotificationManager?.createNotification { notification ->
                    mPlayerService.startForeground(SusicConstants.NOF_ID, notification)
                    sNotificationOngoing = true
                }
            }
        }
    }

}