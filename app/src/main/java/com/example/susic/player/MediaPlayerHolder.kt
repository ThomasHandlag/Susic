package com.example.susic.player

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.support.v4.media.MediaMetadataCompat
import com.example.susic.PlayerState
import com.example.susic.data.Track
import com.example.susic.ui.home.PostController
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class MediaPlayerHolder :
    MediaPlayer.OnErrorListener,
    MediaPlayer.OnCompletionListener,
    MediaPlayer.OnPreparedListener {

    private lateinit var mPlayerService: PlayerService

    private var MEDIA_DURATION : Int = 0
    //store track info
    private var sMediaMetaDataCompat: MediaMetadataCompat? = null
    //player service call back function
    lateinit var mediaPlayerInterface: MediaPlayerInterface

    lateinit var sPostControllerInterface: PostController

    // Media player
    private lateinit var mediaPlayer: MediaPlayer
    private var mExecutor: ScheduledExecutorService? = null
    private var mSeekBarPositionUpdateTask: Runnable? = null

    var currentSong: Track? = null
    var isPostControllerInit: Boolean = false

    private val playerPosition
        get() = mediaPlayer.currentPosition

    // Media player state/booleans
    val isMediaPlayer get() = ::mediaPlayer.isInitialized

    val isPlaying get() = isMediaPlayer && (state != PlayerState.PAUSE)

    var state = PlayerState.PAUSE
    var isPlay = false

    fun setMusicService(playerService: PlayerService) {
        mediaPlayer = MediaPlayer()
        mPlayerService = playerService
        //TODO declare notification manager here
    }

    fun getMediaMetaDataCompat() = sMediaMetaDataCompat

    fun onRestartSeekBarCallback() {
        if (mExecutor == null) startUpdatingCallbackWithPosition()
    }
//    val callBack : (()->Unit)? = null

    fun onPauseSeekBarCallback() {
        stopUpdatingCallbackWithPosition()
    }

    private fun startUpdatingCallbackWithPosition() {
        if (mSeekBarPositionUpdateTask == null) {
            mSeekBarPositionUpdateTask = Runnable { updateProgressCallbackTask() }
        }
        //execute new thread for updating seekBar
        mExecutor = Executors.newSingleThreadScheduledExecutor()
        mExecutor?.scheduleAtFixedRate(
            mSeekBarPositionUpdateTask!!,
            0,
            1000,
            TimeUnit.MILLISECONDS
        )
    }

    // Reports media playback position to mPlaybackProgressCallback.
    private fun stopUpdatingCallbackWithPosition() {
        mExecutor?.shutdownNow()
        mExecutor = null
        mSeekBarPositionUpdateTask = null
    }

    private fun updateProgressCallbackTask() {
        if (isPlaying && ::mediaPlayerInterface.isInitialized) {
            mediaPlayerInterface.onPositionChanged(mediaPlayer.currentPosition)
        }
    }
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
        mediaPlayer.setDataSource(song?.uri)
        MEDIA_DURATION = mediaPlayer.duration
        mediaPlayer.prepare()
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
    fun play() {
        if (isMediaPlayer) {
            mediaPlayer.start()
            isPlay = true
        }
    }

    fun release() {
        destroyInstance()
    }

    fun resumeOrPause() {
        if (isPlaying) {
            pause()
        }
        else {
            resume()
        }
    }

    fun resetMediaResource(track  : Track) {
        restartMediaPlayer(track)
    }

    private fun pause() {
        with(mediaPlayer) {
           try {
               pause()
           }
           catch (throwable: Throwable) {
               throwable.printStackTrace()
           }
        }
    }
    private fun resume() {
        with(mediaPlayer) {
            try {
                start()
            }
            catch (throwable: Throwable) {
                throwable.printStackTrace()
            }
        }
    }

    fun seekTo(pos: Int) {
        mediaPlayer.seekTo(pos)
    }


    fun stopPlaybackService(stopPlayback: Boolean, fromUser: Boolean, fromFocus: Boolean) {

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

    override fun onCompletion(p0: MediaPlayer?) {
        TODO("Not yet implemented")
    }

}