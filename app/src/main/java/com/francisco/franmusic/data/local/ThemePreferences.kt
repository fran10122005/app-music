package com.francisco.franmusic.data.local

import android.content.Context
import com.francisco.franmusic.ui.theme.AppThemeStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemePreferences(context: Context) {
    private val prefs = context.getSharedPreferences("franmusic_theme_prefs", Context.MODE_PRIVATE)

    private val _currentTheme = MutableStateFlow(
        AppThemeStyle.fromName(prefs.getString(KEY_THEME, AppThemeStyle.NEON_CYBERPUNK.name))
    )
    val currentTheme: StateFlow<AppThemeStyle> = _currentTheme.asStateFlow()

    fun setTheme(theme: AppThemeStyle) {
        prefs.edit().putString(KEY_THEME, theme.name).apply()
        _currentTheme.value = theme
    }

    companion object {
        private const val KEY_THEME = "app_theme_style"
    }
}
