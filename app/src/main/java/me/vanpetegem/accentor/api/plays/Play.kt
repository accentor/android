package me.vanpetegem.accentor.api.plays

import com.github.kittinunf.fuel.httpPost
import java.time.Instant
import me.vanpetegem.accentor.data.authentication.AuthenticationData
import me.vanpetegem.accentor.data.plays.Play
import me.vanpetegem.accentor.util.Result
import me.vanpetegem.accentor.util.jsonBody
import me.vanpetegem.accentor.util.responseObject

data class Arguments(val play: PlayArguments)
data class PlayArguments(val trackId: Int, val playedAt: Instant)

fun create(server: String, authenticationData: AuthenticationData, trackId: Int, playedAt: Instant): Result<Play> {
    return "$server/api/plays".httpPost()
        .set("Accept", "application/json")
        .set("X-Secret", authenticationData.secret)
        .set("X-Device-Id", authenticationData.deviceId)
        .jsonBody(Arguments(PlayArguments(trackId, playedAt)))
        .responseObject<Play>().third
        .fold(
            { play: Play -> Result.Success(play) },
            { e: Throwable -> Result.Error(Exception("Error creating play", e)) },
        )
}
