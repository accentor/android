package me.vanpetegem.accentor.data.codecconversions

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import java.time.Instant

@Dao
abstract class CodecConversionDao {
    open fun getAll(): LiveData<List<CodecConversion>> =
        getAllDbCodecConversions().map { us ->
            us.map { CodecConversion.fromDb(it) }
        }

    @Transaction
    open fun upsertAll(codecconversions: List<CodecConversion>) {
        codecconversions.forEach {
            upsert(DbCodecConversion(it.id, it.name, it.ffmpegParams, it.resultingCodecId, it.fetchedAt))
        }
    }

    open fun getCodecConversionById(id: Int): CodecConversion? {
        val dbCodecConversion = getDbCodecConversionById(id)
        dbCodecConversion ?: return null
        return CodecConversion.fromDb(dbCodecConversion)
    }

    open fun getFirstCodecConversion(): CodecConversion? {
        val dbCodecConversion = getFirstDbCodecConversion()
        dbCodecConversion ?: return null
        return CodecConversion.fromDb(dbCodecConversion)
    }

    @Query("SELECT * FROM codec_conversions ORDER BY id ASC")
    protected abstract fun getAllDbCodecConversions(): LiveData<List<DbCodecConversion>>

    @Upsert
    protected abstract fun upsert(codecConversion: DbCodecConversion)

    @Query("SELECT * FROM codec_conversions WHERE id = :id")
    protected abstract fun getDbCodecConversionById(id: Int): DbCodecConversion?

    @Query("SELECT * FROM codec_conversions ORDER BY id ASC LIMIT 1")
    protected abstract fun getFirstDbCodecConversion(): DbCodecConversion?

    @Query("DELETE FROM codec_conversions WHERE fetched_at < :time")
    abstract fun deleteFetchedBefore(time: Instant)

    @Query("DELETE FROM codec_conversions")
    abstract fun deleteAll()
}
