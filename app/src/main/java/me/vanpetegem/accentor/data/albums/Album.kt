package me.vanpetegem.accentor.data.albums

import java.time.Instant
import java.time.LocalDate

data class Album(
    val id: Int,
    val title: String,
    val release: LocalDate,
    val reviewComment: String?,
    val edition: LocalDate?,
    val editionDescription: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val image: String?,
    val imageType: String?,
    val albumLabels: List<AlbumLabel>,
    val albumArtists: List<AlbumArtist>
)

data class AlbumArtist(
    val artistId: Int,
    val name: String,
    val order: Int,
    val join: String?
)

data class AlbumLabel(
    val labelId: Int,
    val catalogueNumber: String
)