package com.francisco.franmusic

import com.francisco.franmusic.domain.model.Song
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
    fun `test offline filtering predicate`() {
        val localSong = Song(
            id = "local_1",
            title = "Local Track",
            artist = "Local Artist",
            album = "Local Album",
            durationMs = 120000L,
            mediaUri = "content://media/external/audio/media/1",
            isOnline = false,
            isDownloaded = true
        )

        val onlineSongNotDownloaded = Song(
            id = "online_1",
            title = "Online Stream",
            artist = "Online Artist",
            album = "Online Album",
            durationMs = 180000L,
            mediaUri = "https://example.com/stream.mp3",
            isOnline = true,
            isDownloaded = false
        )

        val onlineSongDownloaded = Song(
            id = "online_2",
            title = "Online Saved",
            artist = "Online Artist",
            album = "Online Album",
            durationMs = 200000L,
            mediaUri = "https://example.com/stream2.mp3",
            isOnline = true,
            isDownloaded = true,
            localFilePath = "/data/user/0/com.francisco.franmusic/files/downloads/online_2.mp3"
        )

        val allSongs = listOf(localSong, onlineSongNotDownloaded, onlineSongDownloaded)

        // Offline mode filter: only (!isOnline || isDownloaded)
        val offlineSongs = allSongs.filter { !it.isOnline || it.isDownloaded }

        assertEquals(2, offlineSongs.size)
        assertTrue(offlineSongs.contains(localSong))
        assertTrue(offlineSongs.contains(onlineSongDownloaded))
        assertFalse(offlineSongs.contains(onlineSongNotDownloaded))
    }

    @Test
    fun `test grouping songs by album and artist`() {
        val songs = listOf(
            Song(id = "1", title = "Track A", artist = "Artist 1", album = "Album X", durationMs = 1000L, mediaUri = ""),
            Song(id = "2", title = "Track B", artist = "Artist 1", album = "Album X", durationMs = 1000L, mediaUri = ""),
            Song(id = "3", title = "Track C", artist = "Artist 2", album = "Album Y", durationMs = 1000L, mediaUri = "")
        )

        val albums = songs.groupBy { it.album }
        assertEquals(2, albums.size)
        assertEquals(2, albums["Album X"]?.size)
        assertEquals(1, albums["Album Y"]?.size)

        val artists = songs.groupBy { it.artist }
        assertEquals(2, artists.size)
        assertEquals(2, artists["Artist 1"]?.size)
        assertEquals(1, artists["Artist 2"]?.size)
    }
}
