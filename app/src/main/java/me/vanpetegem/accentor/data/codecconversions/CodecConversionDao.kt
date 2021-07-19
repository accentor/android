package me.vanpetegem.accentor.data.codecconversions

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class CodecConversionDao {

    open fun getAll(): LiveData<List<CodecConversion>> = map(getAllDbCodecConversions()) { us ->
        us.map { CodecConversion(it.id, it.name, it.ffmpegParams, it.resultingCodecId) }
    }

    @Transaction
    open fun replaceAll(codecconversions: List<CodecConversion>) {
        deleteAll()
        codecconversions.forEach { insert(DbCodecConversion(it.id, it.name, it.ffmpegParams, it.resultingCodecId)) }
    }

    open fun getCodecConversionById(id: Int): CodecConversion? {
        val dbCodecConversion = getDbCodecConversionById(id)
        dbCodecConversion ?: return null
        return CodecConversion(
            dbCodecConversion.id,
            dbCodecConversion.name,
            dbCodecConversion.ffmpegParams,
            dbCodecConversion.resultingCodecId
        )
    }

    open fun getFirstCodecConversion(): CodecConversion? {
        val dbCodecConversion = getFirstDbCodecConversion()
        dbCodecConversion ?: return null
        return CodecConversion(
            dbCodecConversion.id,
            dbCodecConversion.name,
            dbCodecConversion.ffmpegParams,
            dbCodecConversion.resultingCodecId
        )
    }

    @Query("SELECT * FROM codec_conversions ORDER BY id ASC")
    protected abstract fun getAllDbCodecConversions(): LiveData<List<DbCodecConversion>>

    @Insert
    protected abstract fun insert(codecConversion: DbCodecConversion)

    @Query("SELECT * FROM codec_conversions WHERE id = :id")
    protected abstract fun getDbCodecConversionById(id: Int): DbCodecConversion?

    @Query("SELECT * FROM codec_conversions ORDER BY id ASC LIMIT 1")
    protected abstract fun getFirstDbCodecConversion(): DbCodecConversion?

    @Query("DELETE FROM codec_conversions")
    abstract fun deleteAll()
}
