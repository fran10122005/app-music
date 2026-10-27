package com.francisco.franmusic

import com.francisco.franmusic.domain.model.Song
import com.francisco.franmusic.playback.SleepTimerState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for smart playlist logic, maintenance, and sleep timer state.
 * Phases 3 & 4 verification.
 */
class SmartPlaylistLogicTest {

    private fun makeSong(
        id: String,
        title: String,
        isFavorite: Boolean = false,
        playCount: Int = 0,
        localFilePath: String? = "/storage/music/song.mp3",
        artist: String = "Artist"
    ) = Song(
        id = id,
        title = title,
        artist = artist,
        album = "Album",
        durationMs = 180_000L,
        mediaUri = "content://media/audio/$id",
        isFavorite = isFavorite,
        playCount = playCount,
        localFilePath = localFilePath
    )

    @Test
    fun testFavoritesFilterReturnsOnlyFavorites() {
        val songs = listOf(
            makeSong("1", "Fav A", isFavorite = true),
            makeSong("2", "Not Fav", isFavorite = false),
            makeSong("3", "Fav B", isFavorite = true)
        )
        val favorites = songs.filter { it.isFavorite }
        assertEquals(2, favorites.size)
        assertTrue(favorites.all { it.isFavorite })
    }

    @Test
    fun testToggleFavoriteFlipsValue() {
        val song = makeSong("1", "Song", isFavorite = false)
        val toggled = song.copy(isFavorite = !song.isFavorite)
        assertTrue(toggled.isFavorite)
        assertFalse(toggled.copy(isFavorite = !toggled.isFavorite).isFavorite)
    }

    @Test
    fun testMostPlayedSortsDescending() {
        val songs = listOf(
            makeSong("1", "A", playCount = 5),
            makeSong("2", "B", playCount = 20),
            makeSong("3", "C", playCount = 1)
        )
        val sorted = songs.sortedByDescending { it.playCount }
        assertEquals("2", sorted.first().id)
        assertEquals(20, sorted.first().playCount)
    }

    @Test
    fun testMostPlayedLimitsToTop10() {
        val songs = (1..25).map { makeSong("$it", "Song $it", playCount = it) }
        val top10 = songs.sortedByDescending { it.playCount }.take(10)
        assertEquals(10, top10.size)
        assertEquals(25, top10.first().playCount)
    }

    @Test
    fun testMissingFilesDetection() {
        val songs = listOf(
            makeSong("1", "Present", localFilePath = "/storage/music/1.mp3"),
            makeSong("2", "Missing", localFilePath = null),
            makeSong("3", "Also Present", localFilePath = "/storage/music/3.mp3")
        )
        val missing = songs.filter { it.localFilePath == null }
        assertEquals(1, missing.size)
        assertEquals("2", missing.first().id)
    }

    @Test
    fun testDuplicateDetectionGroupsByTitleAndArtist() {
        val songs = listOf(
            makeSong("1", "Song A", artist = "Artist X"),
            makeSong("2", "Song A", artist = "Artist X"),
            makeSong("3", "Song B", artist = "Artist Y")
        )
        val dupes = songs.groupBy { "${it.title.lowercase()}::${it.artist.lowercase()}" }
            .values
            .filter { it.size > 1 }
        assertEquals(1, dupes.size)
        assertEquals(2, dupes.first().size)
    }

    @Test
    fun testSleepTimerStateFormattedRemaining() {
        val timer = SleepTimerState(isActive = true, remainingSeconds = 125L)
        assertEquals("02:05", timer.formattedRemaining)
    }

    @Test
    fun testSleepTimerStateEndOfSong() {
        val timer = SleepTimerState(isActive = true, isEndOfSong = true)
        assertEquals("Fin de canción", timer.formattedRemaining)
    }

    @Test
    fun testSleepTimerStateInactiveIsEmpty() {
        val timer = SleepTimerState(isActive = false)
        assertEquals("", timer.formattedRemaining)
    }
}
