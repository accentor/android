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
    val trackArtists: List<TrackArtist>,
    val codecId: Int?,
    val length: Int?,
    val bitrate: Int?,
    val locationId: Int?,
)
