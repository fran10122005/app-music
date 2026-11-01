package com.francisco.franmusic.playback

import android.content.Context
import com.francisco.franmusic.domain.model.LyricLine
import com.francisco.franmusic.domain.model.LyricsData
import com.francisco.franmusic.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.regex.Pattern

data class LyricsUiState(
    val isLoading: Boolean = false,
    val lyricsData: LyricsData? = null,
    val errorMessage: String? = null,
    val showTranslation: Boolean = false
)

class LyricsManager(
    private val context: Context,
    private val onLyricsCached: suspend (songId: String, lyricsText: String, insight: String?) -> Unit
) {
    private val _uiState = MutableStateFlow(LyricsUiState())
    val uiState: StateFlow<LyricsUiState> = _uiState.asStateFlow()

    fun toggleTranslation() {
        _uiState.value = _uiState.value.copy(showTranslation = !_uiState.value.showTranslation)
    }

    suspend fun loadLyrics(song: Song) = withContext(Dispatchers.IO) {
        _uiState.value = LyricsUiState(isLoading = true)

        // 1. Check if database already has cached lyrics
        if (!song.lyrics.isNullOrBlank()) {
            val parsedLines = parseLrcOrPlain(song.lyrics)
            val data = LyricsData(
                songId = song.id,
                lines = parsedLines,
                source = "Caché Local Offline",
                aiInsight = null,
                rawText = song.lyrics
            )
            _uiState.value = LyricsUiState(lyricsData = data, isLoading = false)
            return@withContext
        }

        // 2. Check local storage for companion .lrc or .txt file in same folder
        if (!song.localFilePath.isNullOrBlank()) {
            val audioFile = File(song.localFilePath)
            val baseName = audioFile.parentFile?.let { File(it, audioFile.nameWithoutExtension) }
            val lrcFile = File("${baseName?.path}.lrc")
            val txtFile = File("${baseName?.path}.txt")

            val targetFile = when {
                lrcFile.exists() && lrcFile.length() > 0 -> lrcFile
                txtFile.exists() && txtFile.length() > 0 -> txtFile
                else -> null
            }

            if (targetFile != null) {
                try {
                    val content = targetFile.readText()
                    val parsedLines = parseLrcOrPlain(content)
                    val data = LyricsData(
                        songId = song.id,
                        lines = parsedLines,
                        source = "Archivo local (${targetFile.extension.uppercase()})",
                        rawText = content
                    )
                    onLyricsCached(song.id, content, null)
                    _uiState.value = LyricsUiState(lyricsData = data, isLoading = false)
                    return@withContext
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // 3. Fallback opcional no bloqueante: LRCLIB (servicio abierto de letras)
        val lrclibData = fetchFromLrclib(song)
        if (lrclibData != null) {
            onLyricsCached(song.id, lrclibData.rawText, null)
            _uiState.value = LyricsUiState(lyricsData = lrclibData, isLoading = false)
            return@withContext
        }

        _uiState.value = LyricsUiState(
            isLoading = false,
            lyricsData = null,
            errorMessage = "No se encontró archivo de letra (.lrc / .txt) para esta canción."
        )
    }

    private fun fetchFromLrclib(song: Song): LyricsData? {
        return try {
            val encodedTitle = URLEncoder.encode(cleanTitle(song.title), "UTF-8")
            val encodedArtist = URLEncoder.encode(song.artist, "UTF-8")
            val durationSeconds = song.durationMs / 1000

            val url = URL("https://lrclib.net/api/get?artist_name=$encodedArtist&track_name=$encodedTitle&duration=$durationSeconds")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", "FranMusic/1.0 (Android; Local Music Player)")

            if (conn.responseCode == 200) {
                val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(jsonStr)

                val synced = json.optString("syncedLyrics")
                val plain = json.optString("plainLyrics")
                val raw = if (synced.isNotBlank()) synced else plain

                if (raw.isNotBlank()) {
                    val lines = parseLrcOrPlain(raw)
                    return LyricsData(
                        songId = song.id,
                        lines = lines,
                        source = if (synced.isNotBlank()) "LRCLIB (Sincronizada)" else "LRCLIB",
                        rawText = raw
                    )
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    fun parseLrcOrPlain(content: String): List<LyricLine> {
        val lrcPattern = Pattern.compile("""\[(\d{2}):(\d{2})[.:](\d{2,3})\](.*)""")
        val result = mutableListOf<LyricLine>()

        val lines = content.lines()
        for (line in lines) {
            val matcher = lrcPattern.matcher(line.trim())
            if (matcher.matches()) {
                val min = matcher.group(1)?.toLongOrNull() ?: 0L
                val sec = matcher.group(2)?.toLongOrNull() ?: 0L
                val msStr = matcher.group(3) ?: "0"
                val ms = if (msStr.length == 2) (msStr.toLongOrNull() ?: 0L) * 10 else (msStr.toLongOrNull() ?: 0L)
                val totalMs = (min * 60 + sec) * 1000 + ms
                val text = matcher.group(4)?.trim().orEmpty()
                result.add(LyricLine(timeMs = totalMs, text = text))
            } else {
                val cleanLine = line.trim()
                if (cleanLine.isNotBlank()) {
                    result.add(LyricLine(timeMs = -1L, text = cleanLine))
                }
            }
        }
        return result
    }

    private fun cleanTitle(title: String): String {
        return title.replace(Regex("""\(.*?\)|\[.*?\]"""), "").trim()
    }
}
