package com.francisco.franmusic.domain.repository

import com.francisco.franmusic.domain.model.Album
import com.francisco.franmusic.domain.model.Artist
import com.francisco.franmusic.domain.model.Composer
import com.francisco.franmusic.domain.model.Folder
import com.francisco.franmusic.domain.model.Genre
import com.francisco.franmusic.domain.model.Playlist
import com.francisco.franmusic.domain.model.Song
import com.francisco.franmusic.domain.model.YearGroup
import kotlinx.coroutines.flow.Flow

enum class SearchFilter {
    ALL,
    TITLES,
    ARTISTS,
    ALBUMS,
    FOLDERS,
    GENRES,
    FAVORITES
}

interface MusicRepository {
    val songs: Flow<List<Song>>
    val albums: Flow<List<Album>>
    val artists: Flow<List<Artist>>
    val folders: Flow<List<Folder>>
    val genres: Flow<List<Genre>>
    val yearGroups: Flow<List<YearGroup>>
    val composers: Flow<List<Composer>>
    val playlists: Flow<List<Playlist>>
    val favoriteSongs: Flow<List<Song>>
    val mostPlayedSongs: Flow<List<Song>>

    suspend fun refreshLocalSongs(minDurationMs: Long = 30000L)
    fun searchSongs(query: String, filter: SearchFilter = SearchFilter.ALL): Flow<List<Song>>
    fun getSongsByAlbum(albumTitle: String): Flow<List<Song>>
    fun getSongsByArtist(artistName: String): Flow<List<Song>>
    fun getSongsByFolder(folderPath: String): Flow<List<Song>>
    fun getSongsByGenre(genreName: String): Flow<List<Song>>
    fun getSongsByYear(year: Int): Flow<List<Song>>
    fun getSongsByComposer(composerName: String): Flow<List<Song>>
    suspend fun getSongById(songId: String): Song?

    // Favorites & Play count & Lyrics
    suspend fun toggleFavorite(songId: String, isFavorite: Boolean)
    suspend fun recordSongPlayed(songId: String)
    suspend fun saveLyrics(songId: String, lyrics: String, insight: String?)

    // Metadata Editing & Maintenance (Fases 2 y 3)
    suspend fun updateSongMetadata(
        songId: String,
        title: String,
        artist: String,
        album: String,
        year: Int?,
        genre: String?,
        composer: String?,
        trackNumber: Int
    )
    suspend fun cleanMissingFiles(): Int
    suspend fun detectDuplicates(): List<List<Song>>
    suspend fun deleteSongs(songIds: List<String>)

    // Playlists
    suspend fun createPlaylist(name: String): Long
    suspend fun deletePlaylist(playlist: Playlist)
    suspend fun addSongToPlaylist(playlistId: Long, songId: String)
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: String)
    fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>>
}
