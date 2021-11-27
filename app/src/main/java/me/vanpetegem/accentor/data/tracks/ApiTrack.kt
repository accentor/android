package me.vanpetegem.accentor.data.tracks

import java.time.Instant

data class ApiTrack(
    val id: Int,
    val title: String,
    val normalizedTitle: String,
    val number: Int,
    val albumId: Int,
    val reviewComment: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val genreIds: List<Int>,
    val trackArtists: List<ApiTrackArtist>,
    val codecId: Int?,
    val length: Int?,
    val bitrate: Int?,
    val locationId: Int?,
)

data class ApiTrackArtist(
    val artistId: Int,
    val name: String,
    val normalizedName: String,
    val role: Role,
    val order: Int,
)
