package me.vanpetegem.accentor.data.artists

import java.time.Instant

data class Artist(
    val id: Int,
    val name: String,
    val reviewComment: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val image: String?,
    val image500: String?,
    val image250: String?,
    val image100: String?,
    val imageType: String?
)