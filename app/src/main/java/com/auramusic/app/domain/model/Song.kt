package com.auramusic.app.domain.model

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val trackNumber: Int = 0,
    val artworkUri: String? = null,
    val mediaUri: String,
    val isOnline: Boolean = false,
    val isDownloaded: Boolean = false,
    val localFilePath: String? = null,
    val mimeType: String? = "audio/mpeg",
    val sourceId: String = "local"
) {
    val formattedDuration: String
        get() {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }
}
