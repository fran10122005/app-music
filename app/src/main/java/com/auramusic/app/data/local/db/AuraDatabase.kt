package com.auramusic.app.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.auramusic.app.data.local.db.dao.DownloadDao
import com.auramusic.app.data.local.db.dao.PlaylistDao
import com.auramusic.app.data.local.db.dao.SongDao
import com.auramusic.app.data.local.db.entity.DownloadEntity
import com.auramusic.app.data.local.db.entity.PlaylistEntity
import com.auramusic.app.data.local.db.entity.PlaylistSongCrossRef
import com.auramusic.app.data.local.db.entity.SongEntity

@Database(
    entities = [
        SongEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        DownloadEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun downloadDao(): DownloadDao

    companion object {
        @Volatile
        private var INSTANCE: AuraDatabase? = null

        fun getInstance(context: Context): AuraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AuraDatabase::class.java,
                    "auramusic.db"
                ).fallbackToDestructiveMigration()
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
