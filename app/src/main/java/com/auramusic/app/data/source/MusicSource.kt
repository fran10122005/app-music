package com.auramusic.app.data.source

import com.auramusic.app.domain.model.Album
import com.auramusic.app.domain.model.Artist
import com.auramusic.app.domain.model.Song

interface MusicSource {
    val sourceId: String
    val displayName: String

    suspend fun getSongs(): List<Song>
    suspend fun getAlbums(): List<Album>
    suspend fun getArtists(): List<Artist>
    suspend fun search(query: String): List<Song>
    suspend fun getStreamUrl(songId: String): String?
}
