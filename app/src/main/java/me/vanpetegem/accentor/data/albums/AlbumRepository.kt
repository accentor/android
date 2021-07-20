package me.vanpetegem.accentor.data.albums

import android.util.SparseArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import java.time.LocalDate
import me.vanpetegem.accentor.api.album.index
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository
import me.vanpetegem.accentor.util.Result

class AlbumRepository(
    private val albumDao: AlbumDao,
    private val authenticationRepository: AuthenticationRepository
) {
    val allAlbums: LiveData<List<Album>> = albumDao.getAll()
    val allAlbumsById: LiveData<SparseArray<Album>> = map(allAlbums) {
        val map = SparseArray<Album>()
        it.forEach { a -> map.put(a.id, a) }
        map
    }
    val albumsByReleased: LiveData<List<Album>> = map(allAlbums) {
        val copy = it.toMutableList()
        copy.sortWith({ a1, a2 -> a2.release.compareTo(a1.release) })
        copy
    }
    val albumsByAdded: LiveData<List<Album>> = map(allAlbums) {
        val copy = it.toMutableList()
        copy.sortWith({ a1, a2 -> a2.createdAt.compareTo(a1.createdAt) })
        copy
    }
    val randomAlbums: LiveData<List<Album>> = map(allAlbums) {
        val copy = it.toMutableList()
        copy.shuffle()
        copy
    }

    fun findById(id: Int): LiveData<Album?> = albumDao.findById(id)
    fun getById(id: Int): Album? = albumDao.getAlbumById(id)

    fun findByIds(ids: List<Int>): LiveData<List<Album>> = albumDao.findByIds(ids)

    fun findByDay(day: LocalDate): LiveData<List<Album>> = albumDao.findByDay(day)

    suspend fun refresh(handler: suspend (Result<Unit>) -> Unit) {
        when (val result = index(authenticationRepository.server.value!!, authenticationRepository.authData.value!!)) {
            is Result.Success -> {
                albumDao.replaceAll(result.data)
                handler(Result.Success(Unit))
            }
            is Result.Error -> handler(Result.Error(result.exception))
        }
    }

    suspend fun clear() {
        albumDao.deleteAll()
    }
}
