package com.francisco.franmusic.data.repository

import com.francisco.franmusic.data.local.db.FranMusicDatabase
import com.francisco.franmusic.data.local.db.entity.PlaylistEntity
import com.francisco.franmusic.data.local.db.entity.PlaylistSongCrossRef
import com.francisco.franmusic.data.local.db.entity.SongEntity
import com.francisco.franmusic.data.source.LocalMediaSource
import com.francisco.franmusic.domain.model.Album
import com.francisco.franmusic.domain.model.Artist
import com.francisco.franmusic.domain.model.Folder
import com.francisco.franmusic.domain.model.Playlist
import com.francisco.franmusic.domain.model.Song
import com.francisco.franmusic.domain.repository.MusicRepository
import com.francisco.franmusic.domain.repository.SearchFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MusicRepositoryImpl(
    private val database: FranMusicDatabase,
    private val localSource: LocalMediaSource,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : MusicRepository {

    private val songDao = database.songDao()
    private val playlistDao = database.playlistDao()

    init {
        externalScope.launch {
            refreshLocalSongs()
        }
    }

    override val songs: Flow<List<Song>> = songDao.getAllSongs().map { list ->
        list.map { it.toDomain() }
    }

    override val albums: Flow<List<Album>> = songs.map { songList ->
        songList.groupBy { it.album }
            .map { (albumTitle, albumSongs) ->
                val first = albumSongs.first()
                Album(
                    id = "album_${albumTitle.hashCode()}",
                    title = albumTitle,
                    artist = first.artist,
                    artworkUri = first.artworkUri,
                    songCount = albumSongs.size
                )
            }
            .sortedBy { it.title.lowercase() }
    }

    override val artists: Flow<List<Artist>> = songs.map { songList ->
        songList.groupBy { it.artist }
            .map { (artistName, artistSongs) ->
                val distinctAlbums = artistSongs.map { it.album }.distinct().size
                Artist(
                    id = "artist_${artistName.hashCode()}",
                    name = artistName,
                    songCount = artistSongs.size,
                    albumCount = distinctAlbums,
                    artworkUri = artistSongs.firstOrNull { it.artworkUri != null }?.artworkUri
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    override val folders: Flow<List<Folder>> = songs.map { songList ->
        songList.filter { it.folderPath.isNotBlank() }
            .groupBy { it.folderPath }
            .map { (path, folderSongs) ->
                val name = folderSongs.first().folderName
                Folder(
                    path = path,
                    name = name,
                    songCount = folderSongs.size
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    override val favoriteSongs: Flow<List<Song>> = songDao.getFavoriteSongs().map { list ->
        list.map { it.toDomain() }
    }

    override val mostPlayedSongs: Flow<List<Song>> = songDao.getMostPlayedSongs().map { list ->
        list.map { it.toDomain() }
    }

    override val playlists: Flow<List<Playlist>> = playlistDao.getAllPlaylists().map { list ->
        list.map { it.toDomain() }
    }

    override suspend fun refreshLocalSongs() = withContext(Dispatchers.IO) {
        try {
            val localSongs = localSource.getSongs()
            if (localSongs.isNotEmpty()) {
                val entities = localSongs.map { SongEntity.fromDomain(it) }
                songDao.insertSongs(entities)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun searchSongs(query: String, filter: SearchFilter): Flow<List<Song>> {
        return songDao.searchSongs(query).map { entities ->
            val domainList = entities.map { it.toDomain() }
            when (filter) {
                SearchFilter.ALL -> domainList
                SearchFilter.TITLES -> domainList.filter { it.title.contains(query, ignoreCase = true) }
                SearchFilter.ARTISTS -> domainList.filter { it.artist.contains(query, ignoreCase = true) }
                SearchFilter.ALBUMS -> domainList.filter { it.album.contains(query, ignoreCase = true) }
                SearchFilter.FOLDERS -> domainList.filter { it.folderName.contains(query, ignoreCase = true) }
            }
        }
    }

    override fun getSongsByAlbum(albumTitle: String): Flow<List<Song>> {
        return songDao.getSongsByAlbum(albumTitle).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getSongsByArtist(artistName: String): Flow<List<Song>> {
        return songDao.getSongsByArtist(artistName).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getSongsByFolder(folderPath: String): Flow<List<Song>> {
        return songDao.getSongsByFolder(folderPath).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getSongById(songId: String): Song? = withContext(Dispatchers.IO) {
        songDao.getSongById(songId)?.toDomain()
    }

    override suspend fun toggleFavorite(songId: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        songDao.updateFavorite(songId, isFavorite)
    }

    override suspend fun recordSongPlayed(songId: String) = withContext(Dispatchers.IO) {
        songDao.incrementPlayCount(songId)
    }

    override suspend fun saveLyrics(songId: String, lyrics: String, insight: String?) = withContext(Dispatchers.IO) {
        songDao.updateLyrics(songId, lyrics, insight)
    }

    override suspend fun createPlaylist(name: String): Long = withContext(Dispatchers.IO) {
        playlistDao.insertPlaylist(PlaylistEntity(name = name))
    }

    override suspend fun deletePlaylist(playlist: Playlist) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylist(PlaylistEntity(id = playlist.id, name = playlist.name))
    }

    override suspend fun addSongToPlaylist(playlistId: Long, songId: String) = withContext(Dispatchers.IO) {
        playlistDao.insertSongToPlaylist(
            PlaylistSongCrossRef(playlistId = playlistId, songId = songId)
        )
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: String) = withContext(Dispatchers.IO) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    override fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>> {
        return playlistDao.getSongsForPlaylist(playlistId).map { list ->
            list.map { it.toDomain() }
        }
    }
}
