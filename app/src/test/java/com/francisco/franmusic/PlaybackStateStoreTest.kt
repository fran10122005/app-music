package com.francisco.franmusic

import com.francisco.franmusic.domain.model.Song
import com.francisco.franmusic.playback.SavedPlaybackSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackStateStoreTest {

    @Test
    fun `test SavedPlaybackSession validation`() {
        val validSession = SavedPlaybackSession(
            currentSongId = "song_123",
            queueIds = listOf("song_100", "song_123", "song_105"),
            positionMs = 45000L,
            shuffleEnabled = true,
            repeatMode = 1,
            lastUpdated = 1700000000L
        )
        assertTrue(validSession.isValid)

        val emptySongSession = SavedPlaybackSession(
            currentSongId = "",
            queueIds = listOf("song_100")
        )
        assertFalse(emptySongSession.isValid)

        val nullSongSession = SavedPlaybackSession(
            currentSongId = null,
            queueIds = listOf("song_100")
        )
        assertFalse(nullSongSession.isValid)

        val emptyQueueSession = SavedPlaybackSession(
            currentSongId = "song_1",
            queueIds = emptyList()
        )
        assertFalse(emptyQueueSession.isValid)
    }

    @Test
    fun `test queue restoration and song matching logic`() {
        val session = SavedPlaybackSession(
            currentSongId = "song_2",
            queueIds = listOf("song_1", "song_2", "song_3"),
            positionMs = 15000L
        )

        val availableSongs = listOf(
            Song(id = "song_1", title = "Pista 1", artist = "Artista 1", album = "Album A", durationMs = 180000L, mediaUri = ""),
            Song(id = "song_2", title = "Pista 2", artist = "Artista 2", album = "Album A", durationMs = 210000L, mediaUri = ""),
            Song(id = "song_3", title = "Pista 3", artist = "Artista 1", album = "Album B", durationMs = 195000L, mediaUri = "")
        )

        val idMap = availableSongs.associateBy { it.id }
        val restoredQueue = session.queueIds.mapNotNull { idMap[it] }
        val targetSong = session.currentSongId?.let { idMap[it] }
        val targetIndex = restoredQueue.indexOfFirst { it.id == targetSong?.id }

        assertEquals(3, restoredQueue.size)
        assertEquals("Pista 2", targetSong?.title)
        assertEquals(1, targetIndex)
        assertEquals(15000L, session.positionMs)
    }

    @Test
    fun `test serialization of queue ids string list`() {
        val ids = listOf("track_a", "track_b", "track_c")
        val joined = ids.joinToString(",")
        assertEquals("track_a,track_b,track_c", joined)

        val parsed = joined.split(",")
        assertEquals(ids, parsed)
    }
}
