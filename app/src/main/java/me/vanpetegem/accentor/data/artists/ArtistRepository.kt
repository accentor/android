package me.vanpetegem.accentor.data.artists

import android.util.SparseArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import dagger.Reusable
import me.vanpetegem.accentor.api.artist.index
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository
import me.vanpetegem.accentor.util.Result
import java.time.Instant
import javax.inject.Inject

@Reusable
class ArtistRepository
    @Inject
    constructor(
        private val artistDao: ArtistDao,
        private val authenticationRepository: AuthenticationRepository,
    ) {
        val allArtists: LiveData<List<Artist>> = artistDao.getAll()
        val allArtistsById: LiveData<SparseArray<Artist>> =
            allArtists.map {
                val map = SparseArray<Artist>()
                it.forEach { a -> map.put(a.id, a) }
                map
            }
        val artistsByAdded: LiveData<List<Artist>> =
            allArtists.map {
                val copy = it.toMutableList()
                copy.sortWith { a1, a2 -> a2.createdAt.compareTo(a1.createdAt) }
                copy
            }
        val artistsByPlayed: LiveData<List<Artist>> = artistDao.getAllByPlayed()
        val randomArtists: LiveData<List<Artist>> =
            allArtists.map {
                val copy = it.toMutableList()
                copy.shuffle()
                copy
            }

        suspend fun refresh(handler: suspend (Result<Unit>) -> Unit) {
            val fetchStart = Instant.now()

            val toUpsert = ArrayList<Artist>()
            var count = 0
            for (result in index(authenticationRepository.server.value!!, authenticationRepository.authData.value!!)) {
                when (result) {
                    is Result.Success -> {
                        val fetchTime = Instant.now()
                        toUpsert.addAll(result.data.map { Artist.fromApi(it, fetchTime) })
                        count += 1
                        if (count >= 5) {
                            artistDao.upsertAll(toUpsert)
                            toUpsert.clear()
                            count = 0
                        }
                    }
                    is Result.Error -> {
                        handler(Result.Error(result.exception))
                        return
                    }
                }
            }
            artistDao.upsertAll(toUpsert)
            artistDao.deleteFetchedBefore(fetchStart)
            handler(Result.Success(Unit))
        }

        fun clear() {
            artistDao.deleteAll()
        }
    }
