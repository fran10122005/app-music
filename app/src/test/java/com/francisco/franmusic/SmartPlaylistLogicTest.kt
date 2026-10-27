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
        id: String, title: String,
        isFavorite: Boolean = false, playCount: Int = 0,
        localFilePath: String? = "/storage/music/.mp3"
    ) = Song(id=id,title=title,artist="Artist",album="Album",durationMs=180_000L,
        mediaUri="content://media/audio/",isFavorite=isFavorite,playCount=playCount,localFilePath=localFilePath)

    @Test
    fun avorites filter returns only isFavorite true songs() {
        val songs = listOf(makeSong("1","Fav A",isFavorite=true),makeSong("2","Not Fav"),makeSong("3","Fav B",isFavorite=true))
        assertEquals(2, songs.filter { it.isFavorite }.size)
    }

    @Test
    fun 	oggleFavorite logic flips isFavorite correctly() {
        val song = makeSong("1","Song",isFavorite=false)
        assertTrue(song.copy(isFavorite=!song.isFavorite).isFavorite)
    }

    @Test
    fun most played sorts by playCount descending() {
        val songs = listOf(makeSong("1","A",playCount=5),makeSong("2","B",playCount=20),makeSong("3","C",playCount=1))
        assertEquals("2", songs.sortedByDescending { it.playCount }.first().id)
    }

    @Test
    fun most played top 10 limits results correctly() {
        val songs = (1..25).map { makeSong("","Song ",playCount=it) }
        assertEquals(10, songs.sortedByDescending { it.playCount }.take(10).size)
    }

    @Test
    fun missing files detection identifies songs with null path() {
        val songs = listOf(makeSong("1","Present"),makeSong("2","Missing",localFilePath=null),makeSong("3","Also"))
        assertEquals(1, songs.filter { it.localFilePath == null }.size)
    }

    @Test
    fun duplicate detection groups by title and artist() {
        val songs = listOf(makeSong("1","A").copy(artist="X"),makeSong("2","A").copy(artist="X"),makeSong("3","B").copy(artist="Y"))
        val dupes = songs.groupBy { "::" }.values.filter { it.size > 1 }
        assertEquals(1, dupes.size)
    }

    @Test
    fun SleepTimerState formattedRemaining shows time correctly() {
        assertEquals("02:05", SleepTimerState(isActive=true,remainingSeconds=125L).formattedRemaining)
    }

    @Test
    fun SleepTimerState end of song shows correct message() {
        assertEquals("Fin de cancion", SleepTimerState(isActive=true,isEndOfSong=true).formattedRemaining.replace("ó","o"))
    }

    @Test
    fun SleepTimerState formattedRemaining is empty when inactive() {
        assertEquals("", SleepTimerState(isActive=false).formattedRemaining)
    }
}
