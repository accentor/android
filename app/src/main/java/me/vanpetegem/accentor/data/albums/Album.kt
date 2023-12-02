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
    val albumArtists: List<AlbumArtist>,
    val fetchedAt: Instant,
) {
    fun stringifyAlbumArtists() = albumArtists.sortedBy { aa -> aa.order }.fold("") { acc, aa -> acc + aa.name + (aa.separator ?: "") }

    fun firstCharacter() = String(intArrayOf(title.codePointAt(0)), 0, 1)

    fun compareToByName(other: Album): Int {
        var order = this.normalizedTitle.compareTo(other.normalizedTitle)
        order = if (order == 0) this.release.compareTo(other.release) else order
        order = if (order == 0) compareAlbumEditions(this, other) else order
        order = if (order == 0) this.id - other.id else order
        return order
    }

    companion object {
        fun fromDb(
            a: DbAlbum,
            labels: List<AlbumLabel>,
            artists: List<AlbumArtist>,
        ): Album =
            Album(
                a.id,
                a.title,
                a.normalizedTitle,
                a.release,
                a.reviewComment,
                a.edition,
                a.editionDescription,
                a.createdAt,
                a.updatedAt,
                a.image,
                a.image500,
                a.image250,
                a.image100,
                a.imageType,
                labels,
                artists,
                a.fetchedAt,
            )

        fun fromApi(
            a: ApiAlbum,
            fetchTime: Instant,
        ) = Album(
            a.id,
            a.title,
            a.normalizedTitle,
            a.release,
            a.reviewComment,
            a.edition,
            a.editionDescription,
            a.createdAt,
            a.updatedAt,
            a.image,
            a.image500,
            a.image250,
            a.image100,
            a.imageType,
            a.albumLabels,
            a.albumArtists,
            fetchTime,
        )
    }
}

fun compareAlbumEditions(
    a1: Album,
    a2: Album,
): Int {
    if (a1.edition == null && a2.edition == null) {
        return 0
    }
    if (a1.edition == null) {
        return -1
    }
    if (a2.edition == null) {
        return 1
    }
    val order = a1.edition.compareTo(a2.edition)
    if (order != 0) {
        return order
    }
    if (a1.editionDescription == null && a2.editionDescription == null) {
        return 0
    }
    if (a1.editionDescription == null) {
        return -1
    }
    if (a2.editionDescription == null) {
        return 1
    }
    return a1.editionDescription.compareTo(a2.editionDescription)
}

data class AlbumArtist(
    val artistId: Int,
    val name: String,
    val normalizedName: String,
    val order: Int,
    val separator: String?,
)

data class AlbumLabel(
    val labelId: Int,
    val catalogueNumber: String?,
)
