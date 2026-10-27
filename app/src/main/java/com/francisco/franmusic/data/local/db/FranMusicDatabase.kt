package com.francisco.franmusic.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.francisco.franmusic.data.local.db.dao.PlaylistDao
import com.francisco.franmusic.data.local.db.dao.SongDao
import com.francisco.franmusic.data.local.db.entity.PlaylistEntity
import com.francisco.franmusic.data.local.db.entity.PlaylistSongCrossRef
import com.francisco.franmusic.data.local.db.entity.SongEntity

@Database(
    entities = [
        SongEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class
    ],
    version = 4,
    exportSchema = false
)
abstract class FranMusicDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        @Volatile
        private var INSTANCE: FranMusicDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE songs ADD COLUMN genre TEXT DEFAULT NULL")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE songs ADD COLUMN year INTEGER DEFAULT NULL")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE songs ADD COLUMN composer TEXT DEFAULT NULL")
                } catch (_: Exception) {}
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE songs ADD COLUMN lyrics TEXT DEFAULT NULL")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE songs ADD COLUMN aiLyricsInsight TEXT DEFAULT NULL")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE songs ADD COLUMN genre TEXT DEFAULT NULL")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE songs ADD COLUMN year INTEGER DEFAULT NULL")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE songs ADD COLUMN composer TEXT DEFAULT NULL")
                } catch (_: Exception) {}
            }
        }

        fun getInstance(context: Context): FranMusicDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FranMusicDatabase::class.java,
                    "FranMusic.db"
                ).addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                 .fallbackToDestructiveMigration()
                 .fallbackToDestructiveMigrationOnDowngrade()
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
