package com.thugbn.susic.service

import android.media.audiofx.Equalizer
import android.media.audiofx.Visualizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manager for audio effects (Equalizer and Visualizer)
 */
class AudioEffectsManager(private val audioSessionId: Int) {

    private var equalizer: Equalizer? = null
    private var visualizer: Visualizer? = null

    private val _equalizerBands = MutableStateFlow<List<EqualizerBand>>(emptyList())
    val equalizerBands: StateFlow<List<EqualizerBand>> = _equalizerBands.asStateFlow()

    private val _waveform = MutableStateFlow<ByteArray>(ByteArray(0))
    val waveform: StateFlow<ByteArray> = _waveform.asStateFlow()

    private val _fft = MutableStateFlow<ByteArray>(ByteArray(0))
    val fft: StateFlow<ByteArray> = _fft.asStateFlow()

    private val _isVisualizerEnabled = MutableStateFlow(false)
    val isVisualizerEnabled: StateFlow<Boolean> = _isVisualizerEnabled.asStateFlow()

    init {
        initializeEqualizer()
        initializeVisualizer()
    }

    private fun initializeEqualizer() {
        try {
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = true

                // Get equalizer bands info
                val bands = mutableListOf<EqualizerBand>()
                val numBands = numberOfBands.toInt()
                val minLevel = bandLevelRange[0].toInt()
                val maxLevel = bandLevelRange[1].toInt()

                for (i in 0 until numBands) {
                    val centerFreq = getCenterFreq(i.toShort())
                    val currentLevel = getBandLevel(i.toShort()).toInt()
                    bands.add(
                        EqualizerBand(
                            index = i,
                            frequency = centerFreq / 1000, // Convert to Hz
                            minLevel = minLevel,
                            maxLevel = maxLevel,
                            currentLevel = currentLevel
                        )
                    )
                }
                _equalizerBands.value = bands
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initializeVisualizer() {
        try {
            visualizer = Visualizer(audioSessionId).apply {
                captureSize = Visualizer.getCaptureSizeRange()[1] // Max capture size

                // Waveform data capture
                setDataCaptureListener(
                    object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(
                            visualizer: Visualizer?,
                            waveform: ByteArray?,
                            samplingRate: Int
                        ) {
                            waveform?.let { _waveform.value = it }
                        }

                        override fun onFftDataCapture(
                            visualizer: Visualizer?,
                            fft: ByteArray?,
                            samplingRate: Int
                        ) {
                            fft?.let { _fft.value = it }
                        }
                    },
                    Visualizer.getMaxCaptureRate() / 2, // Update rate
                    true, // Waveform
                    true  // FFT
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setBandLevel(bandIndex: Int, level: Int) {
        try {
            equalizer?.setBandLevel(bandIndex.toShort(), level.toShort())

            // Update the band in state
            val updatedBands = _equalizerBands.value.toMutableList()
            updatedBands[bandIndex] = updatedBands[bandIndex].copy(currentLevel = level)
            _equalizerBands.value = updatedBands
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setEqualizerEnabled(enabled: Boolean) {
        equalizer?.enabled = enabled
    }

    fun setVisualizerEnabled(enabled: Boolean) {
        try {
            if (enabled) {
                visualizer?.enabled = true
            } else {
                visualizer?.enabled = false
            }
            _isVisualizerEnabled.value = enabled
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun usePreset(presetIndex: Int) {
        try {
            equalizer?.usePreset(presetIndex.toShort())
            // Update bands state
            val updatedBands = _equalizerBands.value.mapIndexed { index, band ->
                val level = equalizer?.getBandLevel(index.toShort())?.toInt() ?: band.currentLevel
                band.copy(currentLevel = level)
            }
            _equalizerBands.value = updatedBands
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getPresets(): List<String> {
        return try {
            val numPresets = equalizer?.numberOfPresets?.toInt() ?: 0
            (0 until numPresets).map { i ->
                equalizer?.getPresetName(i.toShort()) ?: "Preset $i"
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun resetEqualizer() {
        _equalizerBands.value.forEach { band ->
            setBandLevel(band.index, 0)
        }
    }

    fun release() {
        try {
            visualizer?.enabled = false
            visualizer?.release()
            equalizer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

/**
 * Represents an equalizer band
 */
data class EqualizerBand(
    val index: Int,
    val frequency: Int, // in Hz
    val minLevel: Int,
    val maxLevel: Int,
    val currentLevel: Int
)

