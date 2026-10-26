package com.auramusic.app.download

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.auramusic.app.domain.model.Song

class DownloadManagerHelper(private val context: Context) {

    private val workManager = WorkManager.getInstance(context)

    fun startDownload(song: Song) {
        val inputData = workDataOf(
            DownloadWorker.KEY_SONG_ID to song.id,
            DownloadWorker.KEY_TITLE to song.title,
            DownloadWorker.KEY_ARTIST to song.artist,
            DownloadWorker.KEY_URL to song.mediaUri
        )

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val downloadRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .addTag("download_${song.id}")
            .build()

        workManager.enqueueUniqueWork(
            "download_${song.id}",
            ExistingWorkPolicy.REPLACE,
            downloadRequest
        )
    }

    fun cancelDownload(songId: String) {
        workManager.cancelUniqueWork("download_$songId")
    }

    companion object {
        @Volatile
        private var INSTANCE: DownloadManagerHelper? = null

        fun getInstance(context: Context): DownloadManagerHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DownloadManagerHelper(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
