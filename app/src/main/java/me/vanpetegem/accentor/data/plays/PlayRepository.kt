package me.vanpetegem.accentor.data.plays

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
    suspend fun refresh(handler: suspend (Result<Unit>) -> Unit) {
        val fetchStart = Instant.now()

        for (result in index(authenticationRepository.server.value!!, authenticationRepository.authData.value!!)) {
            when (result) {
                is Result.Success -> {
                    val fetchTime = Instant.now()
                    playDao.upsertAll(result.data.map { Play.fromApi(it, fetchTime) })
                }
                is Result.Error -> {
                    handler(Result.Error(result.exception))
                    return
                }
            }
        }
        playDao.deleteFetchedBefore(fetchStart)
        handler(Result.Success(Unit))
    }

    private suspend fun reportUnreportedPlays() {
        for (play in unreportedPlayDao.getAllUnreportedPlays()) {
            when (val result = create(authenticationRepository.server.value!!, authenticationRepository.authData.value!!, play.trackId, play.playedAt)) {
                is Result.Success -> {
                    unreportedPlayDao.delete(play)
                    val fetchTime = Instant.now()
                    playDao.insert(Play.fromApi(result.data, fetchTime))
                }
            }
        }
    }

    suspend fun reportPlay(trackId: Int, playedAt: Instant) {
        when (val result = create(authenticationRepository.server.value!!, authenticationRepository.authData.value!!, trackId, playedAt)) {
            is Result.Success -> {
                val fetchTime = Instant.now()
                playDao.insert(Play.fromApi(result.data, fetchTime))
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
