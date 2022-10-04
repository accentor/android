package me.vanpetegem.accentor.data.playlists

import java.time.Instant

data class ApiPlaylist(
    val id: Int,
    val name: String,
    val description: String?,
    val userId: Int,
    val playlistType: PlaylistType,
    val createdAt: Instant,
    val updatedAt: Instant,
    val itemIds: List<Int>,
    val access: Access,
)
