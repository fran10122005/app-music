package com.francisco.franmusic.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.francisco.franmusic.domain.model.DownloadInfo
import com.francisco.franmusic.domain.model.DownloadState

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey
    val songId: String,
    val title: String,
    val artist: String,
    val downloadUrl: String,
    val localFilePath: String?,
    val progress: Int = 0,
    val status: String = DownloadState.QUEUED.name,
    val totalBytes: Long = 0L,
    val downloadedBytes: Long = 0L,
    val errorMessage: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): DownloadInfo = DownloadInfo(
        songId = songId,
        title = title,
        artist = artist,
        progress = progress,
        state = try {
            DownloadState.valueOf(status)
        } catch (_: Exception) {
            DownloadState.FAILED
        },
        localFilePath = localFilePath,
        totalBytes = totalBytes,
        downloadedBytes = downloadedBytes,
        error = errorMessage
    )
}
