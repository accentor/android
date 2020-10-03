package me.vanpetegem.accentor.data.artists

import android.util.SparseArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import me.vanpetegem.accentor.api.artist.index
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository
import me.vanpetegem.accentor.util.Result
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

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

    fun refresh(handler: (Result<Unit>) -> Unit) {
        doAsync {
            when (val result =
                index(authenticationRepository.server.value!!, authenticationRepository.authData.value!!)) {
                is Result.Success -> {
                    artistDao.replaceAll(result.data)

                    uiThread {
                        handler(Result.Success(Unit))
                    }
                }
                is Result.Error -> uiThread {
                    handler(Result.Error(result.exception))
                }
            }
        }
    }

    fun clear() {
        doAsync {
            artistDao.deleteAll()
        }
    }

}
