package com.francisco.franmusic.domain.model

data class YearGroup(
    val year: Int,
    val songCount: Int,
    val decade: String
) {
    val displayName: String
        get() = if (year > 0) year.toString() else "Sin año"
}
