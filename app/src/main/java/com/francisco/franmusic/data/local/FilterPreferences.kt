package com.francisco.franmusic.data.local

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FilterPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("franmusic_filter_prefs", Context.MODE_PRIVATE)

    private val _minDurationSeconds = MutableStateFlow(
        prefs.getInt(KEY_MIN_DURATION, 30)
    )
    val minDurationSeconds: StateFlow<Int> = _minDurationSeconds.asStateFlow()

    fun setMinDurationSeconds(seconds: Int) {
        prefs.edit().putInt(KEY_MIN_DURATION, seconds).apply()
        _minDurationSeconds.value = seconds
    }

    companion object {
        private const val KEY_MIN_DURATION = "filter_min_duration_seconds"
    }
}

