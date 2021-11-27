package me.vanpetegem.accentor.data.artists

import android.util.SparseArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import dagger.Reusable
import java.time.Instant
import javax.inject.Inject
import me.vanpetegem.accentor.api.artist.index
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository
import me.vanpetegem.accentor.util.Result

@Reusable
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
    val artistsByPlayed: LiveData<List<Artist>> = artistDao.getAllByPlayed()
    val randomArtists: LiveData<List<Artist>> = map(allArtists) {
        val copy = it.toMutableList()
        copy.shuffle()
        copy
    }

    suspend fun refresh(handler: suspend (Result<Unit>) -> Unit) {
        val fetchStart = Instant.now()

        for (result in index(authenticationRepository.server.value!!, authenticationRepository.authData.value!!)) {
            when (result) {
                is Result.Success -> {
                    val fetchTime = Instant.now()
                    artistDao.upsertAll(result.data.map { Artist.fromApi(it, fetchTime) })
                }
                is Result.Error -> {
                    handler(Result.Error(result.exception))
                    return
                }
            }
        }
        artistDao.deleteFetchedBefore(fetchStart)
        handler(Result.Success(Unit))
    }

    suspend fun clear() {
        artistDao.deleteAll()
    }
}
