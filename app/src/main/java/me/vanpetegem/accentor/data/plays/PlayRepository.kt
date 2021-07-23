package me.vanpetegem.accentor.data.plays

import java.time.Instant
import javax.inject.Inject
import me.vanpetegem.accentor.api.plays.create
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository
import me.vanpetegem.accentor.util.Result

class PlayRepository @Inject constructor(
    private val authenticationRepository: AuthenticationRepository,
    private val unreportedPlayDao: UnreportedPlayDao,
) {
    suspend fun reportPlay(trackId: Int, playedAt: Instant) {
        when (create(authenticationRepository.server.value!!, authenticationRepository.authData.value!!, trackId, playedAt)) {
            is Result.Success -> {
                for (play in unreportedPlayDao.getAllUnreportedPlays()) {
                    when (create(authenticationRepository.server.value!!, authenticationRepository.authData.value!!, play.trackId, play.playedAt)) {
                        is Result.Success -> unreportedPlayDao.delete(play)
                    }
                }
            }
            is Result.Error -> {
                unreportedPlayDao.insert(UnreportedPlay(trackId = trackId, playedAt = playedAt))
            }
        }
    }
}
