package com.francisco.franmusic.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.francisco.franmusic.data.local.db.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE isDownloaded = 1 OR isOnline = 0 ORDER BY title ASC")
    fun getOfflineSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE id = :songId")
    suspend fun getSongById(songId: String): SongEntity?

    @Query("SELECT * FROM songs WHERE id = :songId")
    fun observeSongById(songId: String): Flow<SongEntity?>

    @Query("SELECT * FROM songs WHERE album = :album ORDER BY trackNumber ASC, title ASC")
    fun getSongsByAlbum(album: String): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE artist = :artist ORDER BY title ASC")
    fun getSongsByArtist(artist: String): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE folderPath = :folderPath ORDER BY title ASC")
    fun getSongsByFolder(folderPath: String): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE genre = :genre ORDER BY title ASC")
    fun getSongsByGenre(genre: String): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE genre IS NULL OR genre = '' OR genre = 'Desconocido' ORDER BY title ASC")
    fun getUnclassifiedGenreSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE year = :year ORDER BY title ASC")
    fun getSongsByYear(year: Int): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE year IS NULL OR year = 0 ORDER BY title ASC")
    fun getUnknownYearSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE composer = :composer ORDER BY title ASC")
    fun getSongsByComposer(composer: String): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE composer IS NULL OR composer = '' OR composer = 'Desconocido' ORDER BY title ASC")
    fun getUnknownComposerSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE playCount > 0 ORDER BY playCount DESC, title ASC LIMIT 50")
    fun getMostPlayedSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' OR album LIKE '%' || :query || '%'")
    fun searchSongs(query: String): Flow<List<SongEntity>>

    @Query("UPDATE songs SET title = :title, artist = :artist, album = :album, year = :year, genre = :genre, composer = :composer, trackNumber = :trackNumber WHERE id = :songId")
    suspend fun updateMetadata(songId: String, title: String, artist: String, album: String, year: Int?, genre: String?, composer: String?, trackNumber: Int)

    @Query("DELETE FROM songs WHERE id IN (:ids)")
    suspend fun deleteSongsByIds(ids: List<String>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity)

    @Update
    suspend fun updateSong(song: SongEntity)

    @Query("UPDATE songs SET isDownloaded = :isDownloaded, localFilePath = :filePath WHERE id = :songId")
    suspend fun updateDownloadStatus(songId: String, isDownloaded: Boolean, filePath: String?)

    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE id = :songId")
    suspend fun updateFavorite(songId: String, isFavorite: Boolean)

    @Query("UPDATE songs SET playCount = playCount + 1 WHERE id = :songId")
    suspend fun incrementPlayCount(songId: String)

    @Query("UPDATE songs SET lyrics = :lyrics, aiLyricsInsight = :insight WHERE id = :songId")
    suspend fun updateLyrics(songId: String, lyrics: String?, insight: String?)

    @Delete
    suspend fun deleteSong(song: SongEntity)

    @Query("DELETE FROM songs WHERE isOnline = 0")
    suspend fun clearLocalSongs()
}
