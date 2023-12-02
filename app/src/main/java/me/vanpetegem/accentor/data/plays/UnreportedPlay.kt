package me.vanpetegem.accentor.data.plays

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "unreported_plays")
data class UnreportedPlay(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "track_id")
    val trackId: Int,
    @ColumnInfo(name = "played_at")
    val playedAt: Instant,
)
