package me.vanpetegem.accentor.data.playlists

import androidx.lifecycle.LiveData
import dagger.Reusable
import java.time.Instant
import javax.inject.Inject
import me.vanpetegem.accentor.api.playlist.index
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository
import me.vanpetegem.accentor.util.Result

@Reusable
class PlaylistRepository @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val authenticationRepository: AuthenticationRepository,
) {
    val allPlaylists: LiveData<List<Playlist>> = playlistDao.getAll()

    suspend fun refresh(handler: suspend (Result<Unit>) -> Unit) {
        val fetchStart = Instant.now()

        var toUpsert = ArrayList<Playlist>()
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

    suspend fun clear() {
        playlistDao.deleteAll()
    }
}
