package com.francisco.franmusic.playback

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class SleepTimerState(
    val isActive: Boolean = false,
    val remainingSeconds: Long = 0L,
    val isEndOfSong: Boolean = false
) {
    val formattedRemaining: String
        get() {
            if (!isActive) return ""
            if (isEndOfSong) return "Fin de canción"
            val m = remainingSeconds / 60
            val s = remainingSeconds % 60
            return "%02d:%02d".format(m, s)
        }
}

class SleepTimerManager(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main),
    private val onTimerFinished: () -> Unit
) {
    private var timerJob: Job? = null
    private val _state = MutableStateFlow(SleepTimerState())
    val state: StateFlow<SleepTimerState> = _state.asStateFlow()

    fun startTimerMinutes(minutes: Int) {
        startTimerSeconds(minutes * 60L, isEndOfSong = false)
    }

    fun startEndOfSong() {
        timerJob?.cancel()
        _state.value = SleepTimerState(isActive = true, remainingSeconds = 0L, isEndOfSong = true)
    }

    fun onSongCompleted() {
        if (_state.value.isActive && _state.value.isEndOfSong) {
            cancel()
            onTimerFinished()
        }
    }

    fun startTimerSeconds(seconds: Long, isEndOfSong: Boolean = false) {
        timerJob?.cancel()
        _state.value = SleepTimerState(isActive = true, remainingSeconds = seconds, isEndOfSong = isEndOfSong)

        timerJob = scope.launch {
            var currentSec = seconds
            while (isActive && currentSec > 0) {
                delay(1000)
                currentSec -= 1
                _state.value = _state.value.copy(remainingSeconds = currentSec)
            }
            if (isActive && currentSec <= 0) {
                _state.value = SleepTimerState()
                onTimerFinished()
            }
        }
    }

    fun cancel() {
        timerJob?.cancel()
        timerJob = null
        _state.value = SleepTimerState()
    }
}
