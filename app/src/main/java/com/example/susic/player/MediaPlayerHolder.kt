package com.example.susic.player

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import com.example.susic.PlayerState
import com.example.susic.data.PlayerViewModel
import com.example.susic.data.Track
import com.example.susic.ui.home.PostController

class MediaPlayerHolder :
    MediaPlayer.OnErrorListener,
    MediaPlayer.OnCompletionListener,
    MediaPlayer.OnPreparedListener {

    private lateinit var mPlayerService: PlayerService
    var sMediaDuration: Int = 0
    var sReadyToPlay = false
    var currentPostIndex: Int? = null

    //handler seekbar task
    var sTaskHandler: Handler? = null

    //store track info
    private var sMediaMetaDataCompat: MediaMetadataCompat? = null

    //player service call back function
    lateinit var mediaPlayerInterface: MediaPlayerInterface

    lateinit var sPostControllerInterface: PostController

    // Media player
    private lateinit var mediaPlayer: MediaPlayer
    private var mSeekBarPositionUpdateTask: Runnable? = null

    var currentSong: Track? = null

    val playerPosition
        get() = mediaPlayer.currentPosition

    // Media player state/booleans
    private val isMediaPlayer get() = ::mediaPlayer.isInitialized

    val isPlaying get() = isMediaPlayer && (state != PlayerState.PAUSE)

    var state = PlayerState.PAUSE

    fun setMusicService(playerService: PlayerService) {
        sTaskHandler = Handler(Looper.getMainLooper())
        mediaPlayer = MediaPlayer()
        mPlayerService = playerService
    }

    fun getMediaMetaDataCompat() = sMediaMetaDataCompat

//    val callBack : (()->Unit)? = null


    fun stopPlaybackService(
        stopPlayback: Boolean,
        fromUser: Boolean,
        fromFocus: Boolean
    ) {

    }

    private fun startUpdatingCallbackWithPosition() {
        if (mSeekBarPositionUpdateTask == null) {
            updateProgressCallbackTask()
        }
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
            mediaPlayer.reset(); true
        }
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        mediaPlayer.release()
        //restore media player
        restartMediaPlayer(currentSong)
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
//        INSTANCE = null
    }

    override fun onCompletion(player: MediaPlayer?) {
        sPostControllerInterface.onComplete()
        mediaPlayer.reset()
    }

}