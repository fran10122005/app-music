package com.francisco.franmusic.data.source

import com.francisco.franmusic.domain.model.Album
import com.francisco.franmusic.domain.model.Artist
import com.francisco.franmusic.domain.model.Song

interface MusicSource {
    val sourceId: String
    val displayName: String

    suspend fun getSongs(): List<Song>
    suspend fun getAlbums(): List<Album>
    suspend fun getArtists(): List<Artist>
    suspend fun search(query: String): List<Song>
    suspend fun getStreamUrl(songId: String): String?
}
