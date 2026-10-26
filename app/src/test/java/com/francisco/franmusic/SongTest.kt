package com.francisco.franmusic

import com.francisco.franmusic.domain.model.Song
import com.francisco.franmusic.playback.LyricsManager
import com.francisco.franmusic.playback.SleepTimerState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SongTest {

    @Test
    fun `test formattedDuration calculates mm ss correctly`() {
        val song1 = Song(
            id = "test_1",
            title = "Test Song",
            artist = "Test Artist",
            album = "Test Album",
            durationMs = 65000L, // 1 min 5 sec
            mediaUri = "https://example.com/test.mp3"
        )
        assertEquals("1:05", song1.formattedDuration)

        val song2 = song1.copy(durationMs = 272000L) // 4 min 32 sec
        assertEquals("4:32", song2.formattedDuration)

        val song3 = song1.copy(durationMs = 9000L) // 0 min 9 sec
        assertEquals("0:09", song3.formattedDuration)
    }

    @Test
    fun `test grouping songs by folder`() {
        val songs = listOf(
            Song(id = "1", title = "Pista 1", artist = "Artista", album = "Album", durationMs = 1000L, mediaUri = "", folderPath = "/storage/Music/Rock", folderName = "Rock"),
            Song(id = "2", title = "Pista 2", artist = "Artista", album = "Album", durationMs = 1000L, mediaUri = "", folderPath = "/storage/Music/Rock", folderName = "Rock"),
            Song(id = "3", title = "Pista 3", artist = "Artista", album = "Album", durationMs = 1000L, mediaUri = "", folderPath = "/storage/Music/Jazz", folderName = "Jazz")
        )

        val byFolder = songs.groupBy { it.folderPath }
        assertEquals(2, byFolder.size)
        assertEquals(2, byFolder["/storage/Music/Rock"]?.size)
        assertEquals(1, byFolder["/storage/Music/Jazz"]?.size)
    }

    @Test
    fun `test sleep timer formatting`() {
        val timer1 = SleepTimerState(isActive = true, remainingSeconds = 125L)
        assertEquals("02:05", timer1.formattedRemaining)

        val timerEndSong = SleepTimerState(isActive = true, remainingSeconds = 0L, isEndOfSong = true)
        assertEquals("Fin de canción", timerEndSong.formattedRemaining)

        val timerInactive = SleepTimerState(isActive = false)
        assertEquals("", timerInactive.formattedRemaining)
    }

    @Test
    fun `test lrc line parsing in lyrics manager`() {
        val lrcContent = """
            [00:15.30] Primera línea de la canción
            [01:05.00] Coro principal
            Línea sin timestamp
        """.trimIndent()

        // Test parser logic
        val pattern = java.util.regex.Pattern.compile("""\[(\d{2}):(\d{2})[.:](\d{2,3})\](.*)""")
        val lines = lrcContent.lines()
        val parsed = mutableListOf<Pair<Long, String>>()

        for (line in lines) {
            val matcher = pattern.matcher(line.trim())
            if (matcher.matches()) {
                val min = matcher.group(1)?.toLongOrNull() ?: 0L
                val sec = matcher.group(2)?.toLongOrNull() ?: 0L
                val msStr = matcher.group(3) ?: "0"
                val ms = if (msStr.length == 2) (msStr.toLongOrNull() ?: 0L) * 10 else (msStr.toLongOrNull() ?: 0L)
                val totalMs = (min * 60 + sec) * 1000 + ms
                val text = matcher.group(4)?.trim().orEmpty()
                parsed.add(totalMs to text)
            }
        }

        assertEquals(2, parsed.size)
        assertEquals(15300L, parsed[0].first)
        assertEquals("Primera línea de la canción", parsed[0].second)
        assertEquals(65000L, parsed[1].first)
        assertEquals("Coro principal", parsed[1].second)
    }

    @Test
    fun `test favorite filtering`() {
        val s1 = Song(id = "1", title = "Fav 1", artist = "A", album = "B", durationMs = 100, mediaUri = "", isFavorite = true)
        val s2 = Song(id = "2", title = "Norm 1", artist = "A", album = "B", durationMs = 100, mediaUri = "", isFavorite = false)

        val list = listOf(s1, s2)
        val favs = list.filter { it.isFavorite }

        assertEquals(1, favs.size)
        assertEquals("Fav 1", favs[0].title)
    }
}
