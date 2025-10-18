package me.vanpetegem.accentor.data.playlists

import android.util.SparseArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import dagger.Reusable
import me.vanpetegem.accentor.api.playlist.index
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository
import me.vanpetegem.accentor.util.Result
import java.time.Instant
import javax.inject.Inject

@Reusable
class PlaylistRepository
    @Inject
    constructor(
        private val playlistDao: PlaylistDao,
        private val authenticationRepository: AuthenticationRepository,
    ) {
        val allPlaylists: LiveData<List<Playlist>> = playlistDao.getAll()
        val allPlaylistsById: LiveData<SparseArray<Playlist>> =
            allPlaylists.map {
                val map = SparseArray<Playlist>()
                it.forEach { p -> map.put(p.id, p) }
                map
            }

        suspend fun refresh(handler: suspend (Result<Unit>) -> Unit) {
            val fetchStart = Instant.now()

            val toUpsert = ArrayList<Playlist>()
            var count = 0
            for (result in index(authenticationRepository.server.value!!, authenticationRepository.authData.value!!)) {
                when (result) {
                    is Result.Success -> {
                        val fetchTime = Instant.now()
                        toUpsert.addAll(result.data.map { Playlist.fromApi(it, fetchTime) })
                        count += 1
                        if (count >= 5) {
                            playlistDao.upsertAll(toUpsert)
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
            playlistDao.upsertAll(toUpsert)
            playlistDao.deleteFetchedBefore(fetchStart)
            handler(Result.Success(Unit))
        }

        fun clear() {
            playlistDao.deleteAll()
        }
    }
