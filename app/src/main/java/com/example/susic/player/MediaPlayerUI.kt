package com.example.susic.player

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.susic.R
import com.example.susic.databinding.PlayerFloatBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MediaPlayerUI : BottomSheetDialogFragment() {
    private lateinit var bindUI: PlayerFloatBinding
    var onNowPlayingCancelled: (() -> Unit)? = null

    private lateinit var sMediaControllerInterface: MediaControllerInterface
    private lateinit var sUIControlInterface: PlayerUIController

    private val mMediaPlayerHolder get() = MediaPlayerHolder.getInstance()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            sMediaControllerInterface = activity as MediaControllerInterface
            sUIControlInterface = activity as PlayerUIController
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onNowPlayingCancelled?.invoke()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        bindUI = DataBindingUtil.inflate(layoutInflater, R.layout.player_float, container, false)
        return bindUI.root
    }

    override fun onStart() {
        super.onStart()
        //TODO do something when start player
    }

    override fun onStop() {
        super.onStop()
        //TODO do something when close player
    }

    private fun setupView() {
        //TODO set up view for player

    }

    companion object {
        const val TAG = "PlayerUI"
    }
}