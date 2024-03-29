package me.vanpetegem.accentor.data.albums

import android.util.SparseArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import dagger.Reusable
import me.vanpetegem.accentor.api.album.index
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository
import me.vanpetegem.accentor.util.Result
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

@Reusable
class AlbumRepository
    @Inject
    constructor(
        private val albumDao: AlbumDao,
        private val authenticationRepository: AuthenticationRepository,
    ) {
        val allAlbums: LiveData<List<Album>> = albumDao.getAll()
        val allAlbumsById: LiveData<SparseArray<Album>> =
            allAlbums.map {
                val map = SparseArray<Album>()
                it.forEach { a -> map.put(a.id, a) }
                map
            }
        val albumsByReleased: LiveData<List<Album>> =
            allAlbums.map {
                val copy = it.toMutableList()
                copy.sortWith({ a1, a2 -> a2.release.compareTo(a1.release) })
                copy
            }
        val albumsByAdded: LiveData<List<Album>> =
            allAlbums.map {
                val copy = it.toMutableList()
                copy.sortWith({ a1, a2 -> a2.createdAt.compareTo(a1.createdAt) })
                copy
            }
        val albumsByPlayed: LiveData<List<Album>> = albumDao.getAllByPlayed()
        val randomAlbums: LiveData<List<Album>> =
            allAlbums.map {
                val copy = it.toMutableList()
                copy.shuffle()
                copy
            }

        fun findById(id: Int): LiveData<Album?> = albumDao.findById(id)

        fun getById(id: Int): Album? = albumDao.getAlbumById(id)

        fun getByIds(ids: List<Int>): List<Album> = albumDao.getByIds(ids)

        fun findByIds(ids: List<Int>): LiveData<List<Album>> = albumDao.findByIds(ids)

        fun findByDay(day: LocalDate): LiveData<List<Album>> = albumDao.findByDay(day)

        suspend fun refresh(handler: suspend (Result<Unit>) -> Unit) {
            val fetchStart = Instant.now()

            var toUpsert = ArrayList<Album>()
            var count = 0
            for (result in index(authenticationRepository.server.value!!, authenticationRepository.authData.value!!)) {
                when (result) {
                    is Result.Success -> {
                        val fetchTime = Instant.now()
                        toUpsert.addAll(result.data.map { Album.fromApi(it, fetchTime) })
                        count += 1
                        if (count >= 5) {
                            albumDao.upsertAll(toUpsert)
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
            albumDao.upsertAll(toUpsert)
            albumDao.deleteFetchedBefore(fetchStart)
            handler(Result.Success(Unit))
        }

        suspend fun clear() {
            albumDao.deleteAll()
        }
    }
