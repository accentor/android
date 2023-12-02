package me.vanpetegem.accentor.data.albums

import java.time.Instant
import java.time.LocalDate

data class ApiAlbum(
    val id: Int,
    val title: String,
    val normalizedTitle: String,
    val release: LocalDate,
    val reviewComment: String?,
    val edition: LocalDate?,
    val editionDescription: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val image: String?,
    val image500: String?,
    val image250: String?,
    val image100: String?,
    val imageType: String?,
    val albumLabels: List<AlbumLabel>,
    val albumArtists: List<AlbumArtist>,
)
