package me.vanpetegem.accentor.data.codecconversions

import android.util.SparseArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import me.vanpetegem.accentor.api.codecconversion.index
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository
import me.vanpetegem.accentor.util.Result
import java.time.Instant
import javax.inject.Inject

class CodecConversionRepository
    @Inject
    constructor(
        private val codecConversionDao: CodecConversionDao,
        private val authenticationRepository: AuthenticationRepository,
    ) {
        val allCodecConversions: LiveData<List<CodecConversion>> = codecConversionDao.getAll()
        val allCodecConversionsById: LiveData<SparseArray<CodecConversion>> =
            allCodecConversions.map {
                val map = SparseArray<CodecConversion>()
                it.forEach { u -> map.put(u.id, u) }
                map
            }

        fun getFirst(): CodecConversion? = codecConversionDao.getFirstCodecConversion()

        fun getById(id: Int): CodecConversion? = codecConversionDao.getCodecConversionById(id)

        suspend fun refresh(handler: suspend (Result<Unit>) -> Unit) {
            val fetchStart = Instant.now()

            for (result in index(authenticationRepository.server.value!!, authenticationRepository.authData.value!!)) {
                when (result) {
                    is Result.Success -> {
                        val fetchTime = Instant.now()
                        codecConversionDao.upsertAll(result.data.map { CodecConversion.fromApi(it, fetchTime) })
                    }
                    is Result.Error -> {
                        handler(Result.Error(result.exception))
                        return
                    }
                }
            }
            codecConversionDao.deleteFetchedBefore(fetchStart)
            handler(Result.Success(Unit))
        }

        suspend fun clear() {
            codecConversionDao.deleteAll()
        }
    }
