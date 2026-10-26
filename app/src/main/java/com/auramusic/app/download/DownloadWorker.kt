package com.auramusic.app.download

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.auramusic.app.R
import com.auramusic.app.data.local.db.AuraDatabase
import com.auramusic.app.data.local.db.entity.DownloadEntity
import com.auramusic.app.domain.model.DownloadState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class DownloadWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val songId = inputData.getString(KEY_SONG_ID) ?: return@withContext Result.failure()
        val title = inputData.getString(KEY_TITLE) ?: "Canción"
        val artist = inputData.getString(KEY_ARTIST) ?: "Artista"
        val downloadUrl = inputData.getString(KEY_URL) ?: return@withContext Result.failure()

        val db = AuraDatabase.getInstance(context)
        val downloadDao = db.downloadDao()
        val songDao = db.songDao()

        val notificationId = songId.hashCode()
        createNotificationChannel()

        val initialDownload = DownloadEntity(
            songId = songId,
            title = title,
            artist = artist,
            downloadUrl = downloadUrl,
            localFilePath = null,
            progress = 0,
            status = DownloadState.DOWNLOADING.name
        )
        downloadDao.insertOrUpdate(initialDownload)

        // Set initial foreground info
        try {
            setForeground(createForegroundInfo(title, 0, notificationId))
        } catch (_: Exception) {}

        val downloadsDir = File(context.filesDir, "downloads").apply { if (!exists()) mkdirs() }
        val safeFileName = songId.replace(Regex("[^a-zA-Z0-9_]"), "_") + ".mp3"
        val destinationFile = File(downloadsDir, safeFileName)

        var connection: HttpURLConnection? = null
        try {
            val url = URL(downloadUrl)
            connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 30000
            connection.instanceFollowRedirects = true
            connection.connect()

            if (connection.responseCode !in 200..299) {
                downloadDao.insertOrUpdate(
                    initialDownload.copy(
                        status = DownloadState.FAILED.name,
                        errorMessage = "HTTP ${connection.responseCode}"
                    )
                )
                return@withContext Result.failure()
            }

            val totalBytes = connection.contentLengthLong.takeIf { it > 0 } ?: 10_000_000L
            var downloadedBytes = 0L

            connection.inputStream.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    var lastUpdate = System.currentTimeMillis()

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        if (isStopped) {
                            destinationFile.delete()
                            downloadDao.deleteDownloadById(songId)
                            return@withContext Result.failure()
                        }

                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead

                        val now = System.currentTimeMillis()
                        if (now - lastUpdate > 400) {
                            val progress = ((downloadedBytes * 100) / totalBytes).toInt().coerceIn(0, 99)
                            setProgressAsync(workDataOf("progress" to progress))
                            try {
                                setForeground(createForegroundInfo(title, progress, notificationId))
                            } catch (_: Exception) {}
                            downloadDao.updateProgress(songId, progress, downloadedBytes, DownloadState.DOWNLOADING.name)
                            lastUpdate = now
                        }
                    }
                }
            }

            // Success
            val finalPath = destinationFile.absolutePath
            downloadDao.insertOrUpdate(
                DownloadEntity(
                    songId = songId,
                    title = title,
                    artist = artist,
                    downloadUrl = downloadUrl,
                    localFilePath = finalPath,
                    progress = 100,
                    status = DownloadState.COMPLETED.name,
                    totalBytes = downloadedBytes,
                    downloadedBytes = downloadedBytes
                )
            )

            songDao.updateDownloadStatus(songId, true, finalPath)
            notificationManager.cancel(notificationId)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            if (destinationFile.exists()) {
                destinationFile.delete()
            }
            downloadDao.insertOrUpdate(
                initialDownload.copy(
                    status = DownloadState.FAILED.name,
                    errorMessage = e.message
                )
            )
            notificationManager.cancel(notificationId)
            Result.failure()
        } finally {
            connection?.disconnect()
        }
    }

    private fun createForegroundInfo(title: String, progress: Int, notificationId: Int): ForegroundInfo {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Descargando $title")
            .setContentText("$progress%")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Descargas AuraMusic",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Progreso de descarga de canciones"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "auramusic_downloads_channel"
        const val KEY_SONG_ID = "key_song_id"
        const val KEY_TITLE = "key_title"
        const val KEY_ARTIST = "key_artist"
        const val KEY_URL = "key_url"
    }
}
