package com.francisco.franmusic.ui.theme

import androidx.compose.ui.graphics.Color

enum class AppThemeStyle(
    val title: String,
    val description: String,
    val primary: Color,
    val primaryLight: Color,
    val secondary: Color,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceVariant: Color,
    val accent: Color
) {
    NEON_CYBERPUNK(
        title = "Fran Neon",
        description = "Negro OLED con destellos cian y violeta neón",
        primary = Color(0xFF00F5D4),
        primaryLight = Color(0xFF70FDF0),
        secondary = Color(0xFF7B2CBF),
        background = Color(0xFF080B11),
        surface = Color(0xFF101622),
        surfaceElevated = Color(0xFF182030),
        surfaceVariant = Color(0xFF222C42),
        accent = Color(0xFFF72585)
    ),
    SUNSET_GLOW(
        title = "Sunset Glow",
        description = "Atmósfera cálida con acentos fuego, ámbar y magenta",
        primary = Color(0xFFFF5400),
        primaryLight = Color(0xFFFF8547),
        secondary = Color(0xFFF72585),
        background = Color(0xFF120814),
        surface = Color(0xFF1E0E22),
        surfaceElevated = Color(0xFF2B1630),
        surfaceVariant = Color(0xFF3B2042),
        accent = Color(0xFFFFB703)
    ),
    MIDNIGHT_BLUE(
        title = "Midnight Blue",
        description = "Elegancia nocturna con zafiro, hielo y navy profundo",
        primary = Color(0xFF3A86FF),
        primaryLight = Color(0xFF6EA8FE),
        secondary = Color(0xFF8338EC),
        background = Color(0xFF060B16),
        surface = Color(0xFF0D162A),
        surfaceElevated = Color(0xFF152240),
        surfaceVariant = Color(0xFF1E3058),
        accent = Color(0xFF00F5D4)
    ),
    EMERALD_GROOVE(
        title = "Emerald Groove",
        description = "Verde esmeralda palpitante y menta de alto contraste",
        primary = Color(0xFF10B981),
        primaryLight = Color(0xFF34D399),
        secondary = Color(0xFF059669),
        background = Color(0xFF06120B),
        surface = Color(0xFF0D1E14),
        surfaceElevated = Color(0xFF152D1E),
        surfaceVariant = Color(0xFF1E3E2A),
        accent = Color(0xFFA3E635)
    ),
    DYNAMIC_ALBUM(
        title = "Fran Violet (Original)",
        description = "Estilo icónico FranMusic con violetas y gradientes suaves",
        primary = Color(0xFF8B5CF6),
        primaryLight = Color(0xFFA78BFA),
        secondary = Color(0xFF06B6D4),
        background = Color(0xFF0D0E15),
        surface = Color(0xFF161722),
        surfaceElevated = Color(0xFF1F202E),
        surfaceVariant = Color(0xFF282A3C),
        accent = Color(0xFFEC4899)
    );

    companion object {
        fun fromName(name: String?): AppThemeStyle {
            return entries.find { it.name == name } ?: NEON_CYBERPUNK
        }
    }
}
