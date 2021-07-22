package me.vanpetegem.accentor.data.codecconversions

import android.util.SparseArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import javax.inject.Inject
import me.vanpetegem.accentor.api.codecconversion.index
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository
import me.vanpetegem.accentor.util.Result

class CodecConversionRepository @Inject constructor(
    private val codecConversionDao: CodecConversionDao,
    private val authenticationRepository: AuthenticationRepository
) {
    val allCodecConversions: LiveData<List<CodecConversion>> = codecConversionDao.getAll()
    val allCodecConversionsById: LiveData<SparseArray<CodecConversion>> = map(allCodecConversions) {
        val map = SparseArray<CodecConversion>()
        it.forEach { u -> map.put(u.id, u) }
        map
    }

    suspend fun refresh(handler: suspend (Result<Unit>) -> Unit) {
        when (val result = index(authenticationRepository.server.value!!, authenticationRepository.authData.value!!)) {
            is Result.Success -> {
                codecConversionDao.replaceAll(result.data)
                handler(Result.Success(Unit))
            }
            is Result.Error -> handler(Result.Error(result.exception))
        }
    }

    suspend fun clear() {
        codecConversionDao.deleteAll()
    }
}
