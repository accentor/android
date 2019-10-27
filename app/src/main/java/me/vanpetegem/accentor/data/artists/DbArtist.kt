package me.vanpetegem.accentor.data.artists

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "artists")
data class DbArtist(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "review_comment")
    val reviewComment: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: Instant,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant,
    @ColumnInfo(name = "image")
    val image: String?,
    @ColumnInfo(name = "image_500")
    val image500: String?,
    @ColumnInfo(name = "image_250")
    val image250: String?,
    @ColumnInfo(name = "image_100")
    val image100: String?,
    @ColumnInfo(name = "image_type")
    val imageType: String?
)