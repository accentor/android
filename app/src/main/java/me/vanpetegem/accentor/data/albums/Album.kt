package me.vanpetegem.accentor.data.albums

import java.time.Instant
import java.time.LocalDate

data class Album(
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
    val albumArtists: List<AlbumArtist>
) {
    fun stringifyAlbumArtists() =
        albumArtists.sortedBy { aa -> aa.order }.fold("") { acc, aa -> acc + aa.name + (aa.separator ?: "") }
}

data class AlbumArtist(
    val artistId: Int,
    val name: String,
    val normalizedName: String,
    val order: Int,
    val separator: String?
)

data class AlbumLabel(
    val labelId: Int,
    val catalogueNumber: String?
)
