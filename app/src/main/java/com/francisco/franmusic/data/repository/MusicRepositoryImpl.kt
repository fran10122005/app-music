package com.francisco.franmusic.data.repository

import com.francisco.franmusic.data.local.db.FranMusicDatabase
import com.francisco.franmusic.data.local.db.entity.PlaylistEntity
import com.francisco.franmusic.data.local.db.entity.PlaylistSongCrossRef
import com.francisco.franmusic.data.local.db.entity.SongEntity
import com.francisco.franmusic.data.source.LocalMediaSource
import com.francisco.franmusic.domain.model.Album
import com.francisco.franmusic.domain.model.Artist
import com.francisco.franmusic.domain.model.Composer
import com.francisco.franmusic.domain.model.Folder
import com.francisco.franmusic.domain.model.Genre
import com.francisco.franmusic.domain.model.Playlist
import com.francisco.franmusic.domain.model.Song
import com.francisco.franmusic.domain.model.YearGroup
import com.francisco.franmusic.domain.repository.MusicRepository
import com.francisco.franmusic.domain.repository.SearchFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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

    override val genres: Flow<List<Genre>> = songs.map { songList ->
        songList.groupBy { it.genre?.takeIf { g -> g.isNotBlank() } ?: "Sin clasificar" }
            .map { (genreName, genreSongs) ->
                Genre(
                    name = genreName,
                    songCount = genreSongs.size,
                    artworkUri = genreSongs.firstOrNull { it.artworkUri != null }?.artworkUri
                )
            }
            .sortedWith(compareBy({ it.name == "Sin clasificar" }, { it.name.lowercase() }))
    }

    override val yearGroups: Flow<List<YearGroup>> = songs.map { songList ->
        songList.groupBy { it.year?.takeIf { y -> y > 0 } ?: 0 }
            .map { (yr, yrSongs) ->
                val decade = if (yr == 0) "Sin clasificar" else "${(yr / 10) * 10}s"
                YearGroup(
                    year = yr,
                    songCount = yrSongs.size,
                    decade = decade
                )
            }
            .sortedWith(compareBy({ it.year == 0 }, { -it.year }))
    }

    override val composers: Flow<List<Composer>> = songs.map { songList ->
        songList.groupBy { it.composer?.takeIf { c -> c.isNotBlank() } ?: "Desconocido" }
            .map { (composerName, composerSongs) ->
                Composer(
                    name = composerName,
                    songCount = composerSongs.size
                )
            }
            .sortedWith(compareBy({ it.name == "Desconocido" }, { it.name.lowercase() }))
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

    override suspend fun refreshLocalSongs(minDurationMs: Long) = withContext(Dispatchers.IO) {
        try {
            if (minDurationMs > 0) {
                songDao.deleteSongsShorterThan(minDurationMs)
            }
            val localSongs = localSource.getSongs(minDurationMs)
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
                SearchFilter.GENRES -> domainList.filter { it.genre?.contains(query, ignoreCase = true) == true }
                SearchFilter.FAVORITES -> domainList.filter { it.isFavorite }
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

    override fun getSongsByGenre(genreName: String): Flow<List<Song>> {
        return if (genreName == "Sin clasificar") {
            songDao.getUnclassifiedGenreSongs().map { list -> list.map { it.toDomain() } }
        } else {
            songDao.getSongsByGenre(genreName).map { list -> list.map { it.toDomain() } }
        }
    }

    override fun getSongsByYear(year: Int): Flow<List<Song>> {
        return if (year == 0) {
            songDao.getUnknownYearSongs().map { list -> list.map { it.toDomain() } }
        } else {
            songDao.getSongsByYear(year).map { list -> list.map { it.toDomain() } }
        }
    }

    override fun getSongsByComposer(composerName: String): Flow<List<Song>> {
        return if (composerName == "Desconocido") {
            songDao.getUnknownComposerSongs().map { list -> list.map { it.toDomain() } }
        } else {
            songDao.getSongsByComposer(composerName).map { list -> list.map { it.toDomain() } }
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

    override suspend fun updateSongMetadata(
        songId: String,
        title: String,
        artist: String,
        album: String,
        year: Int?,
        genre: String?,
        composer: String?,
        trackNumber: Int
    ) = withContext(Dispatchers.IO) {
        songDao.updateMetadata(
            songId = songId,
            title = title.trim(),
            artist = artist.trim(),
            album = album.trim(),
            year = year,
            genre = genre?.trim(),
            composer = composer?.trim(),
            trackNumber = trackNumber
        )
    }

    override suspend fun cleanMissingFiles(): Int = withContext(Dispatchers.IO) {
        val allSongs = songDao.getAllSongs().first()
        val missingIds = allSongs.filter { songEntity ->
            val path = songEntity.localFilePath
            if (path.isNullOrBlank()) {
                false
            } else if (path.startsWith("content://")) {
                false
            } else {
                !File(path).exists()
            }
        }.map { it.id }

        if (missingIds.isNotEmpty()) {
            songDao.deleteSongsByIds(missingIds)
        }
        missingIds.size
    }

    override suspend fun detectDuplicates(): List<List<Song>> = withContext(Dispatchers.IO) {
        val allSongs = songDao.getAllSongs().first().map { it.toDomain() }
        allSongs.groupBy { "${it.title.trim().lowercase()}::${it.artist.trim().lowercase()}" }
            .filter { it.value.size > 1 }
            .values
            .toList()
    }

    override suspend fun deleteSongs(songIds: List<String>) = withContext(Dispatchers.IO) {
        if (songIds.isNotEmpty()) {
            songDao.deleteSongsByIds(songIds)
        }
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
