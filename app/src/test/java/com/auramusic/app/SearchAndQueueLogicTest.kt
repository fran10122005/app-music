package com.auramusic.app

import com.auramusic.app.domain.model.Song
import com.auramusic.app.domain.repository.SearchFilter
import org.junit.Assert.assertEquals
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
            mediaUri = "https://example.com/moonlight.mp3",
            isOnline = true,
            isDownloaded = true
        ),
        Song(
            id = "2",
            title = "Nocturne Op. 9",
            artist = "Chopin",
            album = "Romantic Classics",
            durationMs = 272000L,
            mediaUri = "https://example.com/nocturne.mp3",
            isOnline = true,
            isDownloaded = false
        ),
        Song(
            id = "3",
            title = "My Local Guitar",
            artist = "Local Artist",
            album = "Acoustic Sessions",
            durationMs = 180000L,
            mediaUri = "content://media/external/audio/3",
            isOnline = false,
            isDownloaded = true
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
    fun `test search filter by local, online, and downloaded`() {
        val localFiltered = sampleSongs.filter { !it.isOnline }
        assertEquals(1, localFiltered.size)
        assertEquals("My Local Guitar", localFiltered.first().title)

        val onlineFiltered = sampleSongs.filter { it.isOnline }
        assertEquals(2, onlineFiltered.size)

        val downloadedFiltered = sampleSongs.filter { it.isDownloaded }
        assertEquals(2, downloadedFiltered.size)
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
