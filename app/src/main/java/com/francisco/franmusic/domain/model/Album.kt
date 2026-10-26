package com.francisco.franmusic.domain.model

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val artworkUri: String? = null,
    val songCount: Int = 0,
    val year: Int? = null
)
