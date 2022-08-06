package me.vanpetegem.accentor.data.playlists

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "playlists")
data class DbPlaylist(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "description")
    val description: String?,
    @ColumnInfo(name = "user_id")
    val userId: Int,
    @ColumnInfo(name = "playlist_type")
    val playlistType: PlaylistType,
    @ColumnInfo(name = "created_at")
    val createdAt: Instant,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant,
    @ColumnInfo(name = "access")
    val access: Access,
    @ColumnInfo(name = "fetched_at")
    val fetchedAt: Instant,
)

@Entity(tableName = "playlist_items", primaryKeys = ["playlist_id", "item_id"])
data class DbPlaylistItem(
    @ColumnInfo(name = "playlist_id")
    val playlistId: Int,
    @ColumnInfo(name = "item_id")
    val itemId: Int
)
