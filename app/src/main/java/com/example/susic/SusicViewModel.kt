package com.example.susic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.susic.data.Track

class SusicViewModel: ViewModel() {
    private var _sCurrentMediaType = MutableLiveData<SusicConstances>()

    val sCurrentMediaType: LiveData<SusicConstances>
        get() = _sCurrentMediaType

    private var _sPlayingTrack = MutableLiveData<Track>()

    val sPlayingTrack: LiveData<Track>
        get() = _sPlayingTrack
}