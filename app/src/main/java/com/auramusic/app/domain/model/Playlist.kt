package com.auramusic.app.domain.model

data class Playlist(
    val id: Long,
    val name: String,
    val songCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
