package com.francisco.franmusic.data.source

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.francisco.franmusic.domain.model.Album
import com.francisco.franmusic.domain.model.Artist
import com.francisco.franmusic.domain.model.Folder
import com.francisco.franmusic.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LocalMediaSource(
    private val context: Context
) : MusicSource {

    override val sourceId: String = "local"
    override val displayName: String = "Dispositivo"

    override suspend fun getSongs(): List<Song> = withContext(Dispatchers.IO) {
        val songList = mutableListOf<Song>()
        val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projectionList = mutableListOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.COMPOSER
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            projectionList.add(MediaStore.Audio.Media.GENRE)
        }
        val projection = projectionList.toTypedArray()

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} > 10000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val trackColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                val dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                val yearColumn = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR)
                val composerColumn = cursor.getColumnIndex(MediaStore.Audio.Media.COMPOSER)
                val genreColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    cursor.getColumnIndex(MediaStore.Audio.Media.GENRE)
                } else -1

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "Desconocido"
                    val artist = cursor.getString(artistColumn) ?: "Artista desconocido"
                    val album = cursor.getString(albumColumn) ?: "Álbum desconocido"
                    val duration = cursor.getLong(durationColumn)
                    val track = cursor.getInt(trackColumn)
                    val albumId = cursor.getLong(albumIdColumn)
                    val mimeType = cursor.getString(mimeTypeColumn) ?: "audio/mpeg"
                    val dataPath = if (dataColumn >= 0) cursor.getString(dataColumn) else null

                    val rawYear = if (yearColumn >= 0 && !cursor.isNull(yearColumn)) cursor.getInt(yearColumn) else null
                    val year = if (rawYear != null && rawYear > 0) rawYear else null

                    val rawComposer = if (composerColumn >= 0 && !cursor.isNull(composerColumn)) cursor.getString(composerColumn) else null
                    val composer = if (rawComposer.isNullOrBlank() || rawComposer == "<unknown>") null else rawComposer.trim()

                    var genre: String? = if (genreColumn >= 0 && !cursor.isNull(genreColumn)) cursor.getString(genreColumn)?.trim() else null
                    if (genre.isNullOrBlank() || genre == "<unknown>") {
                        genre = null
                    }

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val artworkUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        albumId
                    ).toString()

                    val (folderPath, folderName) = if (!dataPath.isNullOrEmpty()) {
                        val file = File(dataPath)
                        val parent = file.parent ?: ""
                        val name = file.parentFile?.name ?: "Música"
                        Pair(parent, name)
                    } else {
                        Pair("", "Música")
                    }

                    songList.add(
                        Song(
                            id = "local_$id",
                            title = title,
                            artist = if (artist == "<unknown>") "Artista desconocido" else artist,
                            album = if (album == "<unknown>") "Álbum desconocido" else album,
                            durationMs = duration,
                            trackNumber = track,
                            artworkUri = artworkUri,
                            mediaUri = contentUri.toString(),
                            isOnline = false,
                            isDownloaded = true,
                            localFilePath = dataPath ?: contentUri.toString(),
                            mimeType = mimeType,
                            sourceId = sourceId,
                            folderPath = folderPath,
                            folderName = folderName,
                            genre = genre,
                            year = year,
                            composer = composer
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback query with basic columns if extended projection failed
            try {
                val basicProjection = arrayOf(
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.TRACK,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.MIME_TYPE,
                    MediaStore.Audio.Media.DATA
                )
                context.contentResolver.query(
                    collection,
                    basicProjection,
                    selection,
                    null,
                    sortOrder
                )?.use { cursor ->
                    val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                    val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                    val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                    val durCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                    val trackCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
                    val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                    val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                    val dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idCol)
                        val title = cursor.getString(titleCol) ?: "Desconocido"
                        val artist = cursor.getString(artistCol) ?: "Artista desconocido"
                        val album = cursor.getString(albumCol) ?: "Álbum desconocido"
                        val duration = cursor.getLong(durCol)
                        val trackNumber = cursor.getInt(trackCol)
                        val albumId = cursor.getLong(albumIdCol)
                        val mimeType = cursor.getString(mimeCol) ?: "audio/mpeg"
                        val dataPath = if (dataCol >= 0) cursor.getString(dataCol) else null

                        val contentUri = ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            id
                        )

                        val artworkUri = ContentUris.withAppendedId(
                            Uri.parse("content://media/external/audio/albumart"),
                            albumId
                        ).toString()

                        var folderPath = ""
                        var folderName = "Música"
                        if (dataPath != null) {
                            val file = File(dataPath)
                            val parent = file.parentFile
                            if (parent != null) {
                                folderPath = parent.absolutePath
                                folderName = parent.name
                            }
                        }

                        songList.add(
                            Song(
                                id = "local_$id",
                                title = title,
                                artist = artist,
                                album = album,
                                durationMs = duration,
                                trackNumber = trackNumber,
                                artworkUri = artworkUri,
                                mediaUri = contentUri.toString(),
                                isOnline = false,
                                isDownloaded = true,
                                localFilePath = dataPath ?: contentUri.toString(),
                                mimeType = mimeType,
                                sourceId = sourceId,
                                folderPath = folderPath,
                                folderName = folderName,
                                genre = null,
                                year = null,
                                composer = null
                            )
                        )
                    }
                }
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }

        songList
    }

    override suspend fun getAlbums(): List<Album> = withContext(Dispatchers.IO) {
        val songs = getSongs()
        songs.groupBy { it.album }
            .map { (albumTitle, albumSongs) ->
                val firstSong = albumSongs.first()
                Album(
                    id = "album_${albumTitle.hashCode()}",
                    title = albumTitle,
                    artist = firstSong.artist,
                    artworkUri = firstSong.artworkUri,
                    songCount = albumSongs.size
                )
            }
            .sortedBy { it.title.lowercase() }
    }

    override suspend fun getArtists(): List<Artist> = withContext(Dispatchers.IO) {
        val songs = getSongs()
        songs.groupBy { it.artist }
            .map { (artistName, artistSongs) ->
                val albums = artistSongs.map { it.album }.distinct().size
                Artist(
                    id = "artist_${artistName.hashCode()}",
                    name = artistName,
                    songCount = artistSongs.size,
                    albumCount = albums,
                    artworkUri = artistSongs.firstOrNull { it.artworkUri != null }?.artworkUri
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    suspend fun getFolders(): List<Folder> = withContext(Dispatchers.IO) {
        val songs = getSongs()
        songs.filter { it.folderPath.isNotBlank() }
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

    override suspend fun search(query: String): List<Song> = withContext(Dispatchers.IO) {
        val lower = query.lowercase().trim()
        getSongs().filter {
            it.title.lowercase().contains(lower) ||
            it.artist.lowercase().contains(lower) ||
            it.album.lowercase().contains(lower) ||
            it.folderName.lowercase().contains(lower)
        }
    }

    override suspend fun getStreamUrl(songId: String): String? {
        val songs = getSongs()
        return songs.find { it.id == songId }?.mediaUri
    }
}
