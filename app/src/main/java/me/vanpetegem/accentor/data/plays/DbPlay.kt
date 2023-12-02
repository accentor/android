package me.vanpetegem.accentor.data.plays

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "plays")
data class DbPlay(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "played_at")
    val playedAt: Instant,
    @ColumnInfo(name = "track_id")
    val trackId: Int,
    @ColumnInfo(name = "user_id")
    val userId: Int,
    @ColumnInfo(name = "fetched_at")
    val fetchedAt: Instant,
)
