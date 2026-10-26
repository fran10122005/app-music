package com.francisco.franmusic.domain.model

data class LyricLine(
    val timeMs: Long = -1L,
    val text: String,
    val translation: String? = null
) {
    val isSynced: Boolean get() = timeMs >= 0L
}

data class LyricsData(
    val songId: String,
    val lines: List<LyricLine>,
    val source: String, // "Archivo local (.lrc)", "LRCLIB", "✨ Generado con IA", "Caché Local"
    val aiInsight: String? = null,
    val rawText: String = ""
) {
    val hasSyncedLines: Boolean get() = lines.any { it.isSynced }

    fun currentLineIndex(positionMs: Long): Int {
        if (!hasSyncedLines) return -1
        var activeIndex = -1
        for (i in lines.indices) {
            val line = lines[i]
            if (line.isSynced && line.timeMs <= positionMs) {
                activeIndex = i
            } else if (line.isSynced && line.timeMs > positionMs) {
                break
            }
        }
        return activeIndex
    }
}
