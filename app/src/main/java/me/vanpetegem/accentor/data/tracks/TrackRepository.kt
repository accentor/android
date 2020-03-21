package me.vanpetegem.accentor.data.tracks

import android.util.Log
import android.util.SparseArray
import androidx.core.util.valueIterator
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import me.vanpetegem.accentor.api.track.index
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository
import me.vanpetegem.accentor.util.Result
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class TrackRepository(private val trackDao: TrackDao, private val authenticationRepository: AuthenticationRepository) {
    val allTracks: LiveData<List<Track>> = trackDao.getAll()
    val allTracksById: LiveData<SparseArray<Track>> = map(allTracks) {
        val map = SparseArray<Track>()
        it.forEach { t -> map.put(t.id, t) }
        map
    }
    val allTracksByAlbumId: LiveData<SparseArray<MutableList<Track>>> = map(allTracks) {
        val map = SparseArray<MutableList<Track>>()
        it.forEach { t ->
            val l = map.get(t.albumId, ArrayList())
            l.add(t)
            map.put(t.albumId, l)
        }
        map.also { m -> m.valueIterator().forEach { l -> l.sortBy { t -> t.number } } }
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
