package me.vanpetegem.accentor.data.playlists

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import java.time.Instant

@Dao
abstract class PlaylistDao {
    @Transaction
    open fun upsertAll(playlists: List<Playlist>) {
        playlists.forEach { playlist: Playlist ->
            upsert(
                DbPlaylist(
                    playlist.id,
                    playlist.name,
                    playlist.description,
                    playlist.userId,
                    playlist.playlistType,
                    playlist.createdAt,
                    playlist.updatedAt,
                    playlist.access,
                    playlist.fetchedAt
                )
            )
            deletePlaylistItemsById(playlist.id)
            for (iId in playlist.itemIds) {
                insert(DbPlaylistItem(playlist.id, iId))
            }
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun upsert(playlist: DbPlaylist)

    @Insert
    protected abstract fun insert(playlistItem: DbPlaylistItem)

    @Transaction
    open fun deleteFetchedBefore(time: Instant) {
        deletePlaylistsFetchedBefore(time)
        deleteUnusedPlaylistItems()
    }

    @Query("DELETE FROM playlists WHERE fetched_at < :time")
    protected abstract fun deletePlaylistsFetchedBefore(time: Instant)

    @Query("DELETE FROM playlist_items WHERE playlist_id NOT IN (SELECT id FROM playlists)")
    protected abstract fun deleteUnusedPlaylistItems()

    @Query("DELETE FROM playlist_items WHERE playlist_id = :id")
    protected abstract fun deletePlaylistItemsById(id: Int)

    @Query("DELETE FROM playlists")
    protected abstract fun deleteAllPlaylists()

    @Query("DELETE FROM playlist_items")
    protected abstract fun deleteAllPlaylistItems()

    @Transaction
    open fun deleteAll() {
        deleteAllPlaylists()
        deleteAllPlaylistItems()
    }
}
