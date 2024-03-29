package me.vanpetegem.accentor.data.artists

import java.time.Instant

data class ApiArtist(
    val id: Int,
    val name: String,
    val normalizedName: String,
    val reviewComment: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val image: String?,
    val image500: String?,
    val image250: String?,
    val image100: String?,
    val imageType: String?,
)
