package me.vanpetegem.accentor.data.tracks

import android.util.Log
import android.util.SparseArray
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
                    Log.e("TRACKS", "error getting tracks", result.exception)
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