package com.francisco.franmusic.domain.repository

import com.francisco.franmusic.domain.model.Album
import com.francisco.franmusic.domain.model.Artist
import com.francisco.franmusic.domain.model.Folder
import com.francisco.franmusic.domain.model.Playlist
import com.francisco.franmusic.domain.model.Song
import kotlinx.coroutines.flow.Flow

enum class SearchFilter {
    ALL,
    TITLES,
    ARTISTS,
    ALBUMS,
    FOLDERS
}

interface MusicRepository {
    val songs: Flow<List<Song>>
    val albums: Flow<List<Album>>
    val artists: Flow<List<Artist>>
    val folders: Flow<List<Folder>>
    val playlists: Flow<List<Playlist>>
    val favoriteSongs: Flow<List<Song>>
    val mostPlayedSongs: Flow<List<Song>>

    suspend fun refreshLocalSongs()
    fun searchSongs(query: String, filter: SearchFilter = SearchFilter.ALL): Flow<List<Song>>
    fun getSongsByAlbum(albumTitle: String): Flow<List<Song>>
    fun getSongsByArtist(artistName: String): Flow<List<Song>>
    fun getSongsByFolder(folderPath: String): Flow<List<Song>>
    suspend fun getSongById(songId: String): Song?

    // Favorites & Play count & Lyrics
    suspend fun toggleFavorite(songId: String, isFavorite: Boolean)
    suspend fun recordSongPlayed(songId: String)
    suspend fun saveLyrics(songId: String, lyrics: String, insight: String?)

    // Playlists
    suspend fun createPlaylist(name: String): Long
    suspend fun deletePlaylist(playlist: Playlist)
    suspend fun addSongToPlaylist(playlistId: Long, songId: String)
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: String)
    fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>>
}
