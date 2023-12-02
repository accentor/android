package me.vanpetegem.accentor.data.tracks

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "tracks")
data class DbTrack(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "normalized_title")
    val normalizedTitle: String,
    @ColumnInfo(name = "number")
    val number: Int,
    @ColumnInfo(name = "album_id")
    val albumId: Int,
    @ColumnInfo(name = "review_comment")
    val reviewComment: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: Instant,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant,
    @ColumnInfo(name = "codec_id")
    val codecId: Int?,
    @ColumnInfo(name = "length")
    val length: Int?,
    @ColumnInfo(name = "bitrate")
    val bitrate: Int?,
    @ColumnInfo(name = "location_id")
    val locationId: Int?,
    @ColumnInfo(name = "fetched_at")
    val fetchedAt: Instant,
)

@Entity(tableName = "track_artists", primaryKeys = ["track_id", "artist_id", "name", "role"])
data class DbTrackArtist(
    @ColumnInfo(name = "track_id")
    val trackId: Int,
    @ColumnInfo(name = "artist_id")
    val artistId: Int,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "normalized_name")
    val normalizedName: String,
    @ColumnInfo(name = "role")
    val role: Role,
    @ColumnInfo(name = "order")
    val order: Int,
    @ColumnInfo(name = "hidden")
    val hidden: Boolean,
)

@Entity(tableName = "track_genres", primaryKeys = ["track_id", "genre_id"])
data class DbTrackGenre(
    @ColumnInfo(name = "track_id")
    val trackId: Int,
    @ColumnInfo(name = "genre_id")
    val genreId: Int,
)
