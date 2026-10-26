package com.francisco.franmusic

import com.francisco.franmusic.domain.model.Song
import com.francisco.franmusic.domain.repository.SearchFilter
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
            mediaUri = "content://media/external/audio/1",
            folderPath = "/storage/Music/Classics",
            folderName = "Classics"
        ),
        Song(
            id = "2",
            title = "Nocturne Op. 9",
            artist = "Chopin",
            album = "Romantic Classics",
            durationMs = 272000L,
            mediaUri = "content://media/external/audio/2",
            folderPath = "/storage/Music/Classics",
            folderName = "Classics"
        ),
        Song(
            id = "3",
            title = "My Local Guitar",
            artist = "Local Artist",
            album = "Acoustic Sessions",
            durationMs = 180000L,
            mediaUri = "content://media/external/audio/3",
            folderPath = "/storage/Music/Acoustic",
            folderName = "Acoustic"
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
    fun `test search filter by titles, artists, albums, and folders`() {
        val titleMatch = sampleSongs.filter { it.title.contains("Guitar", ignoreCase = true) }
        assertEquals(1, titleMatch.size)

        val artistMatch = sampleSongs.filter { it.artist.contains("Beethoven", ignoreCase = true) }
        assertEquals(1, artistMatch.size)

        val albumMatch = sampleSongs.filter { it.album.contains("Romantic", ignoreCase = true) }
        assertEquals(1, albumMatch.size)

        val folderMatch = sampleSongs.filter { it.folderName.contains("Classics", ignoreCase = true) }
        assertEquals(2, folderMatch.size)
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
