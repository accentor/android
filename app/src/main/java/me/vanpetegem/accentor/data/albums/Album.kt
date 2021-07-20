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

    fun firstCharacter() = String(intArrayOf(title.codePointAt(0)), 0, 1)

    fun compareToByName(other: Album): Int {
        var order = this.normalizedTitle.compareTo(other.normalizedTitle)
        order = if (order == 0) this.release.compareTo(other.release) else order
        order = if (order == 0) compareAlbumEditions(this, other) else order
        order = if (order == 0) this.id - other.id else order
        return order
    }
}

fun compareAlbumEditions(a1: Album, a2: Album): Int {
    if (a1.edition == null && a2.edition == null) { return 0 }
    if (a1.edition == null) { return -1 }
    if (a2.edition == null) { return 1 }
    val order = a1.edition.compareTo(a2.edition)
    return if (order == 0) a1.editionDescription!!.compareTo(a2.editionDescription!!) else order
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
