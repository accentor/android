package me.vanpetegem.accentor.data.tracks

import java.time.Instant

data class Track(
    val id: Int,
    val title: String,
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
    val locationId: Int?
)

data class TrackArtist(
    val artistId: Int,
    val name: String,
    val role: Role,
    val order: Int
)

enum class Role {
    MAIN,
    PERFORMER,
    COMPOSER,
    CONDUCTOR,
    REMIXER,
    PRODUCER,
    ARRANGER
}