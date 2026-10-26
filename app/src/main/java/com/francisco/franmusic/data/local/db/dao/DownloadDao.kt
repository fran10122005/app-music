package com.francisco.franmusic.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.francisco.franmusic.data.local.db.entity.DownloadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY updatedAt DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE songId = :songId")
    suspend fun getDownloadBySongId(songId: String): DownloadEntity?

    @Query("SELECT * FROM downloads WHERE songId = :songId")
    fun observeDownload(songId: String): Flow<DownloadEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(download: DownloadEntity)

    @Update
    suspend fun updateDownload(download: DownloadEntity)

    @Query("UPDATE downloads SET progress = :progress, downloadedBytes = :downloadedBytes, status = :status, updatedAt = :updatedAt WHERE songId = :songId")
    suspend fun updateProgress(
        songId: String,
        progress: Int,
        downloadedBytes: Long,
        status: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Delete
    suspend fun deleteDownload(download: DownloadEntity)

    @Query("DELETE FROM downloads WHERE songId = :songId")
    suspend fun deleteDownloadById(songId: String)
}
