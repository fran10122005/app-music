package com.auramusic.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.auramusic.app.domain.model.Song

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val trackNumber: Int,
    val artworkUri: String?,
    val mediaUri: String,
    val isOnline: Boolean,
    val isDownloaded: Boolean,
    val localFilePath: String?,
    val mimeType: String?,
    val sourceId: String,
    val addedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Song = Song(
        id = id,
        title = title,
        artist = artist,
        album = album,
        durationMs = durationMs,
        trackNumber = trackNumber,
        artworkUri = artworkUri,
        mediaUri = mediaUri,
        isOnline = isOnline,
        isDownloaded = isDownloaded,
        localFilePath = localFilePath,
        mimeType = mimeType,
        sourceId = sourceId
    )

    companion object {
        fun fromDomain(song: Song): SongEntity = SongEntity(
            id = song.id,
            title = song.title,
            artist = song.artist,
            album = song.album,
            durationMs = song.durationMs,
            trackNumber = song.trackNumber,
            artworkUri = song.artworkUri,
            mediaUri = song.mediaUri,
            isOnline = song.isOnline,
            isDownloaded = song.isDownloaded,
            localFilePath = song.localFilePath,
            mimeType = song.mimeType,
            sourceId = song.sourceId
        )
    }
}
