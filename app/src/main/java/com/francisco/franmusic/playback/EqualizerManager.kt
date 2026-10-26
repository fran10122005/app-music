package com.francisco.franmusic.playback

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class EqualizerBand(
    val index: Int,
    val centerFreqHz: Int,
    val level: Int, // in millibels or percentage
    val minLevel: Int,
    val maxLevel: Int
) {
    val displayFreq: String
        get() = if (centerFreqHz >= 1000) "${centerFreqHz / 1000}k" else "${centerFreqHz}Hz"
}

data class EqualizerUiState(
    val isEnabled: Boolean = true,
    val bassBoostEnabled: Boolean = false,
    val bassBoostStrength: Int = 0, // 0 to 100
    val bands: List<EqualizerBand> = emptyList(),
    val presets: List<String> = emptyList(),
    val selectedPresetIndex: Int = -1
)

class EqualizerManager {
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null

    private val _uiState = MutableStateFlow(createDefaultState())
    val uiState: StateFlow<EqualizerUiState> = _uiState.asStateFlow()

    private fun createDefaultState(): EqualizerUiState {
        // Standard 5-band fallback representation
        val defaultBands = listOf(
            EqualizerBand(0, 60, 0, -1000, 1000),
            EqualizerBand(1, 230, 0, -1000, 1000),
            EqualizerBand(2, 910, 0, -1000, 1000),
            EqualizerBand(3, 3600, 0, -1000, 1000),
            EqualizerBand(4, 14000, 0, -1000, 1000)
        )
        val defaultPresets = listOf("Plano", "Rock", "Pop", "Bajo Potente", "Voz Clara", "Electrónica")
        return EqualizerUiState(
            isEnabled = true,
            bassBoostEnabled = false,
            bassBoostStrength = 0,
            bands = defaultBands,
            presets = defaultPresets,
            selectedPresetIndex = 0
        )
    }

    fun attachToAudioSession(audioSessionId: Int) {
        if (audioSessionId == 0) return
        try {
            release()
            val eq = Equalizer(0, audioSessionId)
            eq.enabled = _uiState.value.isEnabled

            val minLevel = eq.bandLevelRange.getOrNull(0)?.toInt() ?: -1000
            val maxLevel = eq.bandLevelRange.getOrNull(1)?.toInt() ?: 1000
            val numBands = eq.numberOfBands.toInt()

            val bands = (0 until numBands).map { bandIdx ->
                val freq = eq.getCenterFreq(bandIdx.toShort()) / 1000 // Convert mHz to Hz
                val level = eq.getBandLevel(bandIdx.toShort()).toInt()
                EqualizerBand(bandIdx, freq, level, minLevel, maxLevel)
            }

            val numPresets = eq.numberOfPresets.toInt()
            val presetList = (0 until numPresets).map { eq.getPresetName(it.toShort()) }

            equalizer = eq

            val bb = BassBoost(0, audioSessionId)
            bb.enabled = _uiState.value.bassBoostEnabled
            bassBoost = bb

            _uiState.value = _uiState.value.copy(
                bands = bands.ifEmpty { _uiState.value.bands },
                presets = presetList.ifEmpty { _uiState.value.presets }
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setBandLevel(bandIndex: Int, level: Int) {
        try {
            equalizer?.setBandLevel(bandIndex.toShort(), level.toShort())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val updatedBands = _uiState.value.bands.map { band ->
            if (band.index == bandIndex) band.copy(level = level) else band
        }
        _uiState.value = _uiState.value.copy(bands = updatedBands, selectedPresetIndex = -1)
    }

    fun selectPreset(presetIndex: Int) {
        try {
            if (presetIndex in 0 until (equalizer?.numberOfPresets ?: 0)) {
                equalizer?.usePreset(presetIndex.toShort())
                val updatedBands = _uiState.value.bands.map { band ->
                    val currentLevel = equalizer?.getBandLevel(band.index.toShort())?.toInt() ?: band.level
                    band.copy(level = currentLevel)
                }
                _uiState.value = _uiState.value.copy(bands = updatedBands, selectedPresetIndex = presetIndex)
                return
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // Simulated preset values for software/fallback
        val fallbackLevels = when (presetIndex) {
            1 -> listOf(400, 200, -100, 300, 500) // Rock
            2 -> listOf(-100, 200, 500, 200, -100) // Pop
            3 -> listOf(800, 500, 0, -200, -300) // Bajo Potente
            4 -> listOf(-200, 100, 600, 400, 0) // Voz
            5 -> listOf(600, 300, 0, 400, 600) // Electrónica
            else -> listOf(0, 0, 0, 0, 0) // Plano
        }
        val updatedBands = _uiState.value.bands.mapIndexed { idx, band ->
            val lvl = fallbackLevels.getOrElse(idx) { 0 }
            band.copy(level = lvl)
        }
        _uiState.value = _uiState.value.copy(bands = updatedBands, selectedPresetIndex = presetIndex)
    }

    fun toggleBassBoost(enabled: Boolean) {
        try {
            bassBoost?.enabled = enabled
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _uiState.value = _uiState.value.copy(bassBoostEnabled = enabled)
    }

    fun setBassBoostStrength(strengthPercent: Int) {
        val clamped = strengthPercent.coerceIn(0, 100)
        try {
            val roundedStrength = (clamped * 10).toShort() // 0 to 1000
            bassBoost?.setStrength(roundedStrength)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _uiState.value = _uiState.value.copy(bassBoostStrength = clamped)
    }

    fun release() {
        try {
            equalizer?.release()
            equalizer = null
            bassBoost?.release()
            bassBoost = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: EqualizerManager? = null

        fun getInstance(): EqualizerManager {
            return INSTANCE ?: synchronized(this) {
                val instance = EqualizerManager()
                INSTANCE = instance
                instance
            }
        }
    }
}
