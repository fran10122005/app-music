package com.francisco.franmusic.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalAppThemeStyle = staticCompositionLocalOf { AppThemeStyle.NEON_CYBERPUNK }

@Composable
fun FranMusicTheme(
    style: AppThemeStyle = AppThemeStyle.NEON_CYBERPUNK,
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = style.primary,
        onPrimary = TextPrimary,
        primaryContainer = style.surfaceElevated,
        onPrimaryContainer = style.primaryLight,
        secondary = style.secondary,
        onSecondary = TextPrimary,
        tertiary = style.accent,
        background = style.background,
        onBackground = TextPrimary,
        surface = style.surface,
        onSurface = TextPrimary,
        surfaceVariant = style.surfaceElevated,
        onSurfaceVariant = TextSecondary
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = style.background.toArgb()
                it.navigationBarColor = style.background.toArgb()
                WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(it, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    CompositionLocalProvider(LocalAppThemeStyle provides style) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
