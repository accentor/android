package me.vanpetegem.accentor.data.artists

import android.util.SparseArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import javax.inject.Inject
import me.vanpetegem.accentor.api.artist.index
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository
import me.vanpetegem.accentor.util.Result

class ArtistRepository @Inject constructor(
    private val artistDao: ArtistDao,
    private val authenticationRepository: AuthenticationRepository
) {
    val allArtists: LiveData<List<Artist>> = artistDao.getAll()
    val allArtistsById: LiveData<SparseArray<Artist>> = map(allArtists) {
        val map = SparseArray<Artist>()
        it.forEach { a -> map.put(a.id, a) }
        map
    }
    val artistsByAdded: LiveData<List<Artist>> = map(allArtists) {
        val copy = it.toMutableList()
        copy.sortWith({ a1, a2 -> a2.createdAt.compareTo(a1.createdAt) })
        copy
    }
    val randomArtists: LiveData<List<Artist>> = map(allArtists) {
        val copy = it.toMutableList()
        copy.shuffle()
        copy
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
