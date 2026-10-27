package com.francisco.franmusic.domain.model

data class Genre(
    val name: String,
    val songCount: Int,
    val artworkUri: String? = null
)
