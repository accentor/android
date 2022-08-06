package me.vanpetegem.accentor.data.playlists

import java.time.Instant

data class Playlist(
    val id: Int,
    val name: String,
    val description: String?,
    val userId: Int,
    val playlistType: PlaylistType,
    val createdAt: Instant,
    val updatedAt: Instant,
    val itemIds: List<Int>,
    val access: Access,
    val fetchedAt: Instant,
) {
    companion object {
        fun fromDb(p: DbPlaylist, playlistItems: List<Int>) =
            Playlist(
                p.id,
                p.name,
                p.description,
                p.userId,
                p.playlistType,
                p.createdAt,
                p.updatedAt,
                playlistItems,
                p.access,
                p.fetchedAt,
            )

        fun fromApi(p: ApiPlaylist, fetchTime: Instant) =
            Playlist(
                p.id,
                p.name,
                p.description,
                p.userId,
                p.playlistType,
                p.createdAt,
                p.updatedAt,
                p.itemIds,
                p.access,
                fetchTime,
            )
    }
}
