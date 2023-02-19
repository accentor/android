package me.vanpetegem.accentor.data.playlists

import android.util.SparseArray
import java.time.Instant
import me.vanpetegem.accentor.data.albums.Album
import me.vanpetegem.accentor.data.albums.AlbumRepository
import me.vanpetegem.accentor.data.tracks.Track
import me.vanpetegem.accentor.data.tracks.TrackRepository

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
    val fetchedAt: Instant
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
                p.fetchedAt
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
                fetchTime
            )
    }

    fun toTrackAlbumPairs(trackRepository: TrackRepository, albumRepository: AlbumRepository): List<Pair<Track, Album>> {
        return when (playlistType) {
            PlaylistType.TRACK -> {
                val albumMap = SparseArray<Album>()
                val tracks = trackRepository.getByIds(itemIds)
                tracks.forEach {
                    if (albumMap.indexOfKey(it.albumId) < 0) {
                        albumMap.put(it.albumId, albumRepository.getById(it.albumId)!!)
                    }
                }
                tracks.map { Pair(it, albumMap.get(it.albumId)) }
            }
            PlaylistType.ALBUM -> albumRepository.getByIds(itemIds).flatMap { a ->
                trackRepository.getByAlbum(a).map { t -> Pair(t, a) }
            }
            PlaylistType.ARTIST -> {
                val albumMap = SparseArray<Album>()
                itemIds.flatMap { id ->
                    val tracks = trackRepository.getByArtistId(id).toMutableList()
                    tracks.forEach {
                        if (albumMap.indexOfKey(it.albumId) < 0) {
                            albumMap.put(it.albumId, albumRepository.getById(it.albumId)!!)
                        }
                    }
                    tracks.sortWith({ t1, t2 -> t1.compareAlphabetically(t2, albumMap) })
                    tracks.map { Pair(it, albumMap.get(it.albumId)) }
                }
            }
        }
    }
}
