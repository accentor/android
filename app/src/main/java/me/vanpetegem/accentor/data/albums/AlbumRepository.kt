package me.vanpetegem.accentor.data.albums

import android.util.Log
import android.util.SparseArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import me.vanpetegem.accentor.api.album.index
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository
import me.vanpetegem.accentor.util.Result
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class AlbumRepository(private val albumDao: AlbumDao, private val authenticationRepository: AuthenticationRepository) {
    val allAlbums: LiveData<List<Album>> = albumDao.getAll()
    val allAlbumsById: LiveData<SparseArray<Album>> = map(allAlbums) {
        val map = SparseArray<Album>()
        it.forEach { a -> map.put(a.id, a) }
        map
    }


    fun refresh(handler: (Result<Unit>) -> Unit) {
        doAsync {
            when (val result =
                index(authenticationRepository.server.value!!, authenticationRepository.authData.value!!)) {
                is Result.Success -> {

                    albumDao.replaceAll(result.data)

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
            albumDao.deleteAll()
        }
    }
}
