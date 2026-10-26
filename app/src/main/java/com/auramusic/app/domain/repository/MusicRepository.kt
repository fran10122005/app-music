package com.auramusic.app.domain.repository

import com.auramusic.app.domain.model.Album
import com.auramusic.app.domain.model.Artist
import com.auramusic.app.domain.model.DownloadInfo
import com.auramusic.app.domain.model.Playlist
import com.auramusic.app.domain.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

enum class SearchFilter {
    ALL,
    LOCAL,
    ONLINE,
    DOWNLOADED
}

interface MusicRepository {
    val songs: Flow<List<Song>>
    val albums: Flow<List<Album>>
    val artists: Flow<List<Artist>>
    val playlists: Flow<List<Playlist>>
    val downloads: Flow<List<DownloadInfo>>
    val isOfflineMode: StateFlow<Boolean>

    fun setOfflineMode(enabled: Boolean)
    suspend fun refreshLocalSongs()
    suspend fun refreshOnlineSongs()
    fun searchSongs(query: String, filter: SearchFilter = SearchFilter.ALL): Flow<List<Song>>
    fun getSongsByAlbum(albumTitle: String): Flow<List<Song>>
    fun getSongsByArtist(artistName: String): Flow<List<Song>>
    suspend fun getSongById(songId: String): Song?

    // Playlists
    suspend fun createPlaylist(name: String): Long
    suspend fun deletePlaylist(playlist: Playlist)
    suspend fun addSongToPlaylist(playlistId: Long, songId: String)
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: String)
    fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>>

    // Downloads
    suspend fun updateDownloadStatus(songId: String, isDownloaded: Boolean, filePath: String?)
    suspend fun deleteDownload(songId: String)
}
