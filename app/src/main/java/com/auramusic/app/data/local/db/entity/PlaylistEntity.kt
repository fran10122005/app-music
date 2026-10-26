package com.auramusic.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.auramusic.app.domain.model.Playlist

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(songCount: Int = 0): Playlist = Playlist(
        id = id,
        name = name,
        songCount = songCount,
        createdAt = createdAt
    )
}
