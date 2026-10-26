package com.francisco.franmusic.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.francisco.franmusic.data.local.db.dao.DownloadDao
import com.francisco.franmusic.data.local.db.dao.PlaylistDao
import com.francisco.franmusic.data.local.db.dao.SongDao
import com.francisco.franmusic.data.local.db.entity.DownloadEntity
import com.francisco.franmusic.data.local.db.entity.PlaylistEntity
import com.francisco.franmusic.data.local.db.entity.PlaylistSongCrossRef
import com.francisco.franmusic.data.local.db.entity.SongEntity

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
abstract class FranMusicDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun downloadDao(): DownloadDao

    companion object {
        @Volatile
        private var INSTANCE: FranMusicDatabase? = null

        fun getInstance(context: Context): FranMusicDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FranMusicDatabase::class.java,
                    "FranMusic.db"
                ).fallbackToDestructiveMigration()
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
