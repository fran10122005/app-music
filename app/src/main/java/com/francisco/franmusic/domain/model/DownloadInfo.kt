package com.francisco.franmusic.domain.model

enum class DownloadState {
    NOT_DOWNLOADED,
    QUEUED,
    DOWNLOADING,
    COMPLETED,
    FAILED
}

data class DownloadInfo(
    val songId: String,
    val title: String,
    val artist: String,
    val progress: Int = 0,
    val state: DownloadState = DownloadState.NOT_DOWNLOADED,
    val localFilePath: String? = null,
    val totalBytes: Long = 0L,
    val downloadedBytes: Long = 0L,
    val error: String? = null
)
