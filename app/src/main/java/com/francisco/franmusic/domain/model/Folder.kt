package com.francisco.franmusic.domain.model

data class Folder(
    val path: String,
    val name: String,
    val songCount: Int = 0
)
