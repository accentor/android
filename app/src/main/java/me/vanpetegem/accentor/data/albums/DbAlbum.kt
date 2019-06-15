package me.vanpetegem.accentor.data.albums

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "albums")
data class DbAlbum(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "release")
    val release: LocalDate,
    @ColumnInfo(name = "review_comment")
    val reviewComment: String?,
    @ColumnInfo(name = "edition")
    val edition: LocalDate?,
    @ColumnInfo(name = "edition_description")
    val editionDescription: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: Instant,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant,
    @ColumnInfo(name = "image")
    val image: String?,
    @ColumnInfo(name = "image_type")
    val imageType: String?
)

@Entity(
    tableName = "album_artists",
    primaryKeys = ["album_id", "artist_id"]
)
data class DbAlbumArtist(
    @ColumnInfo(name = "album_id")
    val albumId: Int,
    @ColumnInfo(name = "artist_id")
    val artistId: Int,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "order")
    val order: Int,
    @ColumnInfo(name = "join")
    val join: String?
)

@Entity(
    tableName = "album_labels",
    primaryKeys = ["album_id", "label_id"]
)
data class DbAlbumLabel(
    @ColumnInfo(name = "album_id")
    val albumId: Int,
    @ColumnInfo(name = "label_id")
    val labelId: Int,
    @ColumnInfo(name = "catalogue_number")
    val catalogueNumber: String
)