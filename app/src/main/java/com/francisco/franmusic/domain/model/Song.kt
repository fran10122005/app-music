package com.francisco.franmusic.domain.model

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
    val sourceId: String = "local",
    val folderPath: String = "",
    val folderName: String = "Música",
    val isFavorite: Boolean = false,
    val playCount: Int = 0
) {
    val formattedDuration: String
        get() {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }
}
