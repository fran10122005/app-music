package com.auramusic.app.data.source

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.auramusic.app.domain.model.Album
import com.auramusic.app.domain.model.Artist
import com.auramusic.app.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.MIME_TYPE
        )

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

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "Desconocido"
                    val artist = cursor.getString(artistColumn) ?: "Artista desconocido"
                    val album = cursor.getString(albumColumn) ?: "Álbum desconocido"
                    val duration = cursor.getLong(durationColumn)
                    val track = cursor.getInt(trackColumn)
                    val albumId = cursor.getLong(albumIdColumn)
                    val mimeType = cursor.getString(mimeTypeColumn) ?: "audio/mpeg"

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val artworkUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        albumId
                    ).toString()

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
                            localFilePath = contentUri.toString(),
                            mimeType = mimeType,
                            sourceId = sourceId
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
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

    override suspend fun search(query: String): List<Song> = withContext(Dispatchers.IO) {
        val lower = query.lowercase().trim()
        getSongs().filter {
            it.title.lowercase().contains(lower) ||
            it.artist.lowercase().contains(lower) ||
            it.album.lowercase().contains(lower)
        }
    }

    override suspend fun getStreamUrl(songId: String): String? {
        val songs = getSongs()
        return songs.find { it.id == songId }?.mediaUri
    }
}
