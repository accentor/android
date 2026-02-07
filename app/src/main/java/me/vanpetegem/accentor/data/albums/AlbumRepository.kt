package me.vanpetegem.accentor.data.albums

import android.util.SparseArray
import androidx.core.util.containsKey
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import me.vanpetegem.accentor.api.album.index
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository
import me.vanpetegem.accentor.util.Result
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
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
                copy.sortWith { a1, a2 -> a2.release.compareTo(a1.release) }
                copy
            }
        val albumsByAdded: LiveData<List<Album>> =
            allAlbums.map {
                val copy = it.toMutableList()
                copy.sortWith { a1, a2 -> a2.createdAt.compareTo(a1.createdAt) }
                copy
            }
        val albumsByPlayed: LiveData<List<Album>> = albumDao.getIdsByPlayed().switchMap { ids ->
            allAlbumsById.map { albums ->
                ids.filter { albums.containsKey(it) }.map { albums[it] }
            }
        }
        val randomAlbums: LiveData<List<Album>> =
            allAlbums.map {
                val copy = it.toMutableList()
                copy.shuffle()
                copy
            }

        fun findById(id: Int): LiveData<Album?> = allAlbumsById.map { if (it.containsKey(id)) { it[id] } else { null }  }

        fun getById(id: Int): Album? = allAlbumsById.value?.let { if (it.containsKey(id)) { it[id] } else { null }  }

        fun getByIds(ids: List<Int>): List<Album> = allAlbumsById.value?.let { albums ->
                ids.filter { albums.containsKey(it) }.map { albums[it] }
            } ?: emptyList()


        fun findByIds(ids: List<Int>): LiveData<List<Album>> = allAlbumsById.map { albums ->
                ids.filter { albums.containsKey(it) }.map { albums[it] }
            }

        fun findByDay(day: LocalDate): LiveData<List<Album>> = allAlbums.map { albums -> albums.filter { it.release.dayOfMonth == day.dayOfMonth && it.release.month == day.month }.sortedWith { a1, a2 ->
            a1.compareToByRelease(a2)
        } }

        suspend fun refresh(handler: suspend (Result<Unit>) -> Unit) {
            val fetchStart = Instant.now()

            val toUpsert = ArrayList<Album>()
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

        fun clear() {
            albumDao.deleteAll()
        }
    }
