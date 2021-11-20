package me.vanpetegem.accentor.data.plays

import androidx.lifecycle.LiveData
import java.time.Instant
import javax.inject.Inject
import me.vanpetegem.accentor.api.plays.create
import me.vanpetegem.accentor.api.plays.index
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository
import me.vanpetegem.accentor.util.Result

class PlayRepository @Inject constructor(
    private val playDao: PlayDao,
    private val unreportedPlayDao: UnreportedPlayDao,
    private val authenticationRepository: AuthenticationRepository,
) {
    val allPlays: LiveData<List<Play>> = playDao.getAll()

    suspend fun refresh(handler: suspend (Result<Unit>) -> Unit) {
        when (val result = index(authenticationRepository.server.value!!, authenticationRepository.authData.value!!)) {
            is Result.Success -> {
                playDao.replaceAll(result.data)
                handler(Result.Success(Unit))
            }
            is Result.Error -> handler(Result.Error(result.exception))
        }
    }

    private suspend fun reportUnreportedPlays() {
        for (play in unreportedPlayDao.getAllUnreportedPlays()) {
            when (val result = create(authenticationRepository.server.value!!, authenticationRepository.authData.value!!, play.trackId, play.playedAt)) {
                is Result.Success -> {
                    unreportedPlayDao.delete(play)
                    playDao.insert(result.data)
                }
            }
        }
    }

    suspend fun reportPlay(trackId: Int, playedAt: Instant) {
        when (val result = create(authenticationRepository.server.value!!, authenticationRepository.authData.value!!, trackId, playedAt)) {
            is Result.Success -> {
                playDao.insert(result.data)
                reportUnreportedPlays()
            }
            is Result.Error -> {
                unreportedPlayDao.insert(UnreportedPlay(trackId = trackId, playedAt = playedAt))
            }
        }
    }

    suspend fun clear() {
        playDao.deleteAll()
    }
}
