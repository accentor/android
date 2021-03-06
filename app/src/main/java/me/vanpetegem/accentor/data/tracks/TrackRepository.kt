package me.vanpetegem.accentor.data.tracks

import androidx.lifecycle.LiveData
import me.vanpetegem.accentor.api.track.index
import me.vanpetegem.accentor.data.albums.Album
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository
import me.vanpetegem.accentor.util.Result

class TrackRepository(
    private val trackDao: TrackDao,
    private val authenticationRepository: AuthenticationRepository
) {
    fun findById(id: Int): LiveData<Track?> {
        return trackDao.findById(id)
    }

    fun findByIds(ids: List<Int>): LiveData<List<Track>> {
        return trackDao.findByIds(ids)
    }

    fun getByAlbum(album: Album): List<Track> {
        return trackDao.getByAlbum(album)
    }

    suspend fun refresh(handler: suspend (Result<Unit>) -> Unit) {
        when (val result = index(authenticationRepository.server.value!!, authenticationRepository.authData.value!!)) {
            is Result.Success -> {
                trackDao.replaceAll(result.data)
                handler(Result.Success(Unit))
            }
            is Result.Error -> handler(Result.Error(result.exception))
        }
    }

    suspend fun clear() {
        trackDao.deleteAll()
    }
}
