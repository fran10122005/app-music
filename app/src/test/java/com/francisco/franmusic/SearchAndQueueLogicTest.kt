package com.francisco.franmusic

import com.francisco.franmusic.data.source.LocalMediaSource
import com.francisco.franmusic.domain.model.Song
import com.francisco.franmusic.domain.repository.SearchFilter
import com.francisco.franmusic.ui.screens.SongSortOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchAndQueueLogicTest {

    private val sampleSongs = listOf(
        Song(
            id = "1",
            title = "Moonlight Sonata",
            artist = "Beethoven",
            album = "Piano Masterpieces",
            durationMs = 315000L,
            mediaUri = "content://media/external/audio/1",
            folderPath = "/storage/Music/Classics",
            folderName = "Classics",
            genre = "Classical",
            isFavorite = true
        ),
        Song(
            id = "2",
            title = "Nocturne Op. 9",
            artist = "Chopin",
            album = "Romantic Classics",
            durationMs = 272000L,
            mediaUri = "content://media/external/audio/2",
            folderPath = "/storage/Music/Classics",
            folderName = "Classics",
            genre = "Classical",
            isFavorite = false
        ),
        Song(
            id = "3",
            title = "My Local Guitar",
            artist = "Local Artist",
            album = "Acoustic Sessions",
            durationMs = 180000L,
            mediaUri = "content://media/external/audio/3",
            folderPath = "/storage/Music/Acoustic",
            folderName = "Acoustic",
            genre = "Acoustic",
            isFavorite = true
        )
    )

    @Test
    fun `test search by title and artist`() {
        val query = "chopin"
        val results = sampleSongs.filter {
            it.title.lowercase().contains(query) ||
            it.artist.lowercase().contains(query) ||
            it.album.lowercase().contains(query)
        }
        assertEquals(1, results.size)
        assertEquals("Nocturne Op. 9", results.first().title)
    }

    @Test
    fun `test search filter by titles, artists, albums, folders, genres, and favorites`() {
        val titleMatch = sampleSongs.filter { it.title.contains("Guitar", ignoreCase = true) }
        assertEquals(1, titleMatch.size)

        val artistMatch = sampleSongs.filter { it.artist.contains("Beethoven", ignoreCase = true) }
        assertEquals(1, artistMatch.size)

        val albumMatch = sampleSongs.filter { it.album.contains("Romantic", ignoreCase = true) }
        assertEquals(1, albumMatch.size)

        val folderMatch = sampleSongs.filter { it.folderName.contains("Classics", ignoreCase = true) }
        assertEquals(2, folderMatch.size)

        val genreMatch = sampleSongs.filter { it.genre?.contains("Acoustic", ignoreCase = true) == true }
        assertEquals(1, genreMatch.size)

        val favoritesMatch = sampleSongs.filter { it.isFavorite }
        assertEquals(2, favoritesMatch.size)
    }

    @Test
    fun `test exclusion of WhatsApp, Telegram, and voice notes audio paths`() {
        assertTrue(LocalMediaSource.isExcludedPath("/storage/emulated/0/WhatsApp/Media/WhatsApp Audio/AUD-2026.opus"))
        assertTrue(LocalMediaSource.isExcludedPath("/storage/emulated/0/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Voice Notes/202601/PTT.opus"))
        assertTrue(LocalMediaSource.isExcludedPath("/storage/emulated/0/Telegram/Telegram Audio/voice_123.ogg"))
        assertTrue(LocalMediaSource.isExcludedPath("/storage/emulated/0/Ringtones/Marimba.mp3"))
        assertTrue(LocalMediaSource.isExcludedPath("/storage/emulated/0/Notifications/Ping.ogg"))

        assertFalse(LocalMediaSource.isExcludedPath("/storage/emulated/0/Music/Queen - Bohemian Rhapsody.mp3"))
        assertFalse(LocalMediaSource.isExcludedPath("/storage/emulated/0/Download/Rock Classics/Track01.flac"))
    }

    @Test
    fun `test song sorting orders`() {
        val titleAsc = sampleSongs.sortedBy { it.title.lowercase() }
        assertEquals("Moonlight Sonata", titleAsc.first().title)

        val titleDesc = sampleSongs.sortedByDescending { it.title.lowercase() }
        assertEquals("Nocturne Op. 9", titleDesc.first().title)

        val durationDesc = sampleSongs.sortedByDescending { it.durationMs }
        assertEquals("Moonlight Sonata", durationDesc.first().title)

        val durationAsc = sampleSongs.sortedBy { it.durationMs }
        assertEquals("My Local Guitar", durationAsc.first().title)
    }

    @Test
    fun `test queue index and navigation`() {
        val queue = sampleSongs
        val currentIndex = 1

        val hasNext = currentIndex < queue.size - 1
        val hasPrevious = currentIndex > 0

        assertTrue(hasNext)
        assertTrue(hasPrevious)
        assertEquals("My Local Guitar", queue[currentIndex + 1].title)
        assertEquals("Moonlight Sonata", queue[currentIndex - 1].title)
    }
}
