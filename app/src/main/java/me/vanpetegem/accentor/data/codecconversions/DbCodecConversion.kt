package me.vanpetegem.accentor.data.codecconversions

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "codec_conversions")
data class DbCodecConversion(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "ffmpeg_params")
    val ffmpegParams: String,
    @ColumnInfo(name = "resulting_codec_id")
    val resultingCodecId: Int,
    @ColumnInfo(name = "fetched_at")
    val fetchedAt: Instant
)
