package me.vanpetegem.accentor.data.artists

import android.util.SparseArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import me.vanpetegem.accentor.api.artist.index
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository
import me.vanpetegem.accentor.util.Result

class ArtistRepository(
    private val artistDao: ArtistDao,
    private val authenticationRepository: AuthenticationRepository
) {
    val allArtists: LiveData<List<Artist>> = artistDao.getAll()
    val allArtistsById: LiveData<SparseArray<Artist>> = map(allArtists) {
        val map = SparseArray<Artist>()
        it.forEach { a -> map.put(a.id, a) }
        map
    }

    suspend fun refresh(handler: suspend (Result<Unit>) -> Unit) {
        when (val result = index(authenticationRepository.server.value!!, authenticationRepository.authData.value!!)) {
            is Result.Success -> {
                artistDao.replaceAll(result.data)
                handler(Result.Success(Unit))
            }
            is Result.Error -> handler(Result.Error(result.exception))
        }
    }

    suspend fun clear() {
        artistDao.deleteAll()
    }

}
