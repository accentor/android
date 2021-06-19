package me.vanpetegem.accentor.data.albums

import android.util.SparseArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import me.vanpetegem.accentor.api.album.index
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository
import me.vanpetegem.accentor.util.Result

class AlbumRepository(private val albumDao: AlbumDao, private val authenticationRepository: AuthenticationRepository) {
    val allAlbums: LiveData<List<Album>> = albumDao.getAll()
    val allAlbumsById: LiveData<SparseArray<Album>> = map(allAlbums) {
        val map = SparseArray<Album>()
        it.forEach { a -> map.put(a.id, a) }
        map
    }

    fun findById(id: Int): LiveData<Album?> {
        return albumDao.findById(id)
    }

    fun findByIds(ids: List<Int>): LiveData<List<Album>> {
        return albumDao.findByIds(ids)
    }

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
