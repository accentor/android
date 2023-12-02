package me.vanpetegem.accentor.data.playlists

import android.util.SparseArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import java.time.Instant

@Dao
abstract class PlaylistDao {
    open fun getAll(): LiveData<List<Playlist>> =
        getAllDbPlaylists().switchMap { playlists ->
            playlistItemsByPlaylistId().map { playlistItems ->
                playlists.map { p -> Playlist.fromDb(p, playlistItems.get(p.id, ArrayList())) }
            }
        }

    protected open fun playlistItemsByPlaylistId(): LiveData<SparseArray<MutableList<Int>>> =
        getAllPlaylistItems().map {
            val mapping = SparseArray<MutableList<Int>>()
            for (pi in it) {
                val l = mapping.get(pi.playlistId, ArrayList())
                l.add(pi.itemId)
                mapping.put(pi.playlistId, l)
            }
            return@map mapping
        }

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
                    playlist.fetchedAt,
                ),
            )
            deletePlaylistItemsById(playlist.id)
            for (i in 0 until playlist.itemIds.size) {
                insert(DbPlaylistItem(playlist.id, playlist.itemIds[i], i))
            }
        }
    }

    @Upsert
    protected abstract fun upsert(playlist: DbPlaylist)

    @Insert
    protected abstract fun insert(playlistItem: DbPlaylistItem)

    @Transaction
    open fun deleteFetchedBefore(time: Instant) {
        deletePlaylistsFetchedBefore(time)
        deleteUnusedPlaylistItems()
    }

    @Query("SELECT * FROM playlists ORDER BY name ASC, id ASC")
    protected abstract fun getAllDbPlaylists(): LiveData<List<DbPlaylist>>

    @Query("SELECT * FROM playlist_items ORDER BY `order` ASC")
    protected abstract fun getAllPlaylistItems(): LiveData<List<DbPlaylistItem>>

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
