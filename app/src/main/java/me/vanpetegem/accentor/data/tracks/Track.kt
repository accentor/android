package me.vanpetegem.accentor.data.tracks

import android.util.SparseArray
import me.vanpetegem.accentor.data.albums.Album
import java.time.Instant

data class Track(
    val id: Int,
    val title: String,
    val normalizedTitle: String,
    val number: Int,
    val albumId: Int,
    val reviewComment: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val genreIds: List<Int>,
    val trackArtists: List<TrackArtist>,
    val codecId: Int?,
    val length: Int?,
    val bitrate: Int?,
    val locationId: Int?,
    val fetchedAt: Instant,
) {
    fun stringifyTrackArtists() = trackArtists.filter { ta -> !ta.hidden }.sortedBy { ta -> ta.order }.joinToString(" / ") { ta -> ta.name }

    fun compareTo(
        other: Track,
        albums: SparseArray<Album>,
    ): Int {
        val a1 = albums[this.albumId]
        val a2 = albums[other.albumId]
        if (a1 == null && a2 == null) {
            return this.number - other.number
        }
        if (a1 == null) {
            return 1
        }
        if (a2 == null) {
            return -1
        }
        val order = a1.compareToByName(a2)
        if (order != 0) {
            return order
        }
        return this.number - other.number
    }

    fun compareAlphabetically(
        other: Track,
        albums: SparseArray<Album>,
    ): Int {
        var order = normalizedTitle.compareTo(other.normalizedTitle)
        if (order != 0) {
            return order
        }
        order = this.number - other.number
        if (order != 0) {
            return order
        }
        return compareTo(other, albums)
    }

    companion object {
        fun fromDb(
            t: DbTrack,
            trackArtists: List<TrackArtist>,
            trackGenres: List<Int>,
        ) = Track(
            t.id,
            t.title,
            t.normalizedTitle,
            t.number,
            t.albumId,
            t.reviewComment,
            t.createdAt,
            t.updatedAt,
            trackGenres,
            trackArtists,
            t.codecId,
            t.length,
            t.bitrate,
            t.locationId,
            t.fetchedAt,
        )

        fun fromApi(
            t: ApiTrack,
            fetchTime: Instant,
        ) = Track(
            t.id,
            t.title,
            t.normalizedTitle,
            t.number,
            t.albumId,
            t.reviewComment,
            t.createdAt,
            t.updatedAt,
            t.genreIds,
            t.trackArtists,
            t.codecId,
            t.length,
            t.bitrate,
            t.locationId,
            fetchTime,
        )
    }
}

data class TrackArtist(
    val artistId: Int,
    val name: String,
    val normalizedName: String,
    val role: Role,
    val order: Int,
    val hidden: Boolean,
)

enum class Role {
    MAIN,
    PERFORMER,
    COMPOSER,
    CONDUCTOR,
    REMIXER,
    PRODUCER,
    ARRANGER,
}
