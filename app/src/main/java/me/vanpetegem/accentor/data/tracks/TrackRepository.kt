package me.vanpetegem.accentor.data.tracks

import android.util.SparseArray
import androidx.core.util.valueIterator
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import me.vanpetegem.accentor.api.track.index
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository
import me.vanpetegem.accentor.data.albums.Album
import me.vanpetegem.accentor.util.Result
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class TrackRepository(private val trackDao: TrackDao, private val authenticationRepository: AuthenticationRepository) {
    fun findById(id: Int): LiveData<Track?> {
        return trackDao.findById(id)
    }

    fun findByIds(ids: List<Int>): LiveData<List<Track>> {
        return trackDao.findByIds(ids)
    }

    fun getByAlbum(album: Album): List<Track> {
        return trackDao.getByAlbum(album)
    }

    fun refresh(handler: (Result<Unit>) -> Unit) {
        doAsync {
            when (val result =
                index(authenticationRepository.server.value!!, authenticationRepository.authData.value!!)) {
                is Result.Success -> {
                    trackDao.replaceAll(result.data)

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
            trackDao.deleteAll()
        }
    }
}
