package com.francisco.franmusic

import com.francisco.franmusic.domain.model.Composer
import com.francisco.franmusic.domain.model.Genre
import com.francisco.franmusic.domain.model.Song
import com.francisco.franmusic.domain.model.YearGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MetadataNavigationTest {

    private val testSongs = listOf(
        Song(
            id = "1",
            title = "Smooth Criminal",
            artist = "Michael Jackson",
            album = "Bad",
            durationMs = 257000L,
            mediaUri = "content://media/external/audio/1",
            genre = "Pop",
            year = 1987,
            composer = "Michael Jackson"
        ),
        Song(
            id = "2",
            title = "Billie Jean",
            artist = "Michael Jackson",
            album = "Thriller",
            durationMs = 294000L,
            mediaUri = "content://media/external/audio/2",
            genre = "Pop",
            year = 1982,
            composer = "Michael Jackson"
        ),
        Song(
            id = "3",
            title = "Smells Like Teen Spirit",
            artist = "Nirvana",
            album = "Nevermind",
            durationMs = 301000L,
            mediaUri = "content://media/external/audio/3",
            genre = "Grunge",
            year = 1991,
            composer = "Kurt Cobain"
        ),
        Song(
            id = "4",
            title = "Unknown Track Demo",
            artist = "Indie Unknown",
            album = "Tape Sessions",
            durationMs = 120000L,
            mediaUri = "content://media/external/audio/4",
            genre = null,
            year = null,
            composer = null
        )
    )

    @Test
    fun `test genre grouping and unclassified fallback`() {
        val genres = testSongs.groupBy { it.genre?.takeIf { g -> g.isNotBlank() } ?: "Sin clasificar" }
            .map { (name, songs) ->
                Genre(
                    name = name,
                    songCount = songs.size
                )
            }
            .sortedWith(compareBy({ it.name == "Sin clasificar" }, { it.name.lowercase() }))

        assertEquals(3, genres.size)
        assertEquals("Grunge", genres[0].name)
        assertEquals(1, genres[0].songCount)

        assertEquals("Pop", genres[1].name)
        assertEquals(2, genres[1].songCount)

        assertEquals("Sin clasificar", genres[2].name)
        assertEquals(1, genres[2].songCount)
    }

    @Test
    fun `test year grouping and decade grouping`() {
        val yearGroups = testSongs.groupBy { it.year?.takeIf { y -> y > 0 } ?: 0 }
            .map { (yr, songs) ->
                val decade = if (yr == 0) "Sin clasificar" else "${(yr / 10) * 10}s"
                YearGroup(
                    year = yr,
                    songCount = songs.size,
                    decade = decade
                )
            }
            .sortedWith(compareBy({ it.year == 0 }, { -it.year }))

        assertEquals(4, yearGroups.size)
        // 1991 (1990s)
        assertEquals(1991, yearGroups[0].year)
        assertEquals("1990s", yearGroups[0].decade)
        assertEquals(1, yearGroups[0].songCount)

        // 1987 (1980s)
        assertEquals(1987, yearGroups[1].year)
        assertEquals("1980s", yearGroups[1].decade)
        assertEquals(1, yearGroups[1].songCount)

        // 1982 (1980s)
        assertEquals(1982, yearGroups[2].year)
        assertEquals("1980s", yearGroups[2].decade)
        assertEquals(1, yearGroups[2].songCount)

        // Year 0 (Sin clasificar)
        assertEquals(0, yearGroups[3].year)
        assertEquals("Sin año", yearGroups[3].displayName)
        assertEquals("Sin clasificar", yearGroups[3].decade)
        assertEquals(1, yearGroups[3].songCount)
    }

    @Test
    fun `test composer grouping and unknown fallback`() {
        val composers = testSongs.groupBy { it.composer?.takeIf { c -> c.isNotBlank() } ?: "Desconocido" }
            .map { (name, songs) ->
                Composer(
                    name = name,
                    songCount = songs.size
                )
            }
            .sortedWith(compareBy({ it.name == "Desconocido" }, { it.name.lowercase() }))

        assertEquals(3, composers.size)
        assertEquals("Kurt Cobain", composers[0].name)
        assertEquals(1, composers[0].songCount)

        assertEquals("Michael Jackson", composers[1].name)
        assertEquals(2, composers[1].songCount)

        assertEquals("Desconocido", composers[2].name)
        assertEquals(1, composers[2].songCount)
    }
}
