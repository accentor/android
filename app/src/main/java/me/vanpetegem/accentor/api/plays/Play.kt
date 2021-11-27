package me.vanpetegem.accentor.api.plays

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import java.time.Instant
import me.vanpetegem.accentor.data.authentication.AuthenticationData
import me.vanpetegem.accentor.data.plays.ApiPlay
import me.vanpetegem.accentor.util.Result
import me.vanpetegem.accentor.util.jsonBody
import me.vanpetegem.accentor.util.responseObject

data class Arguments(val play: PlayArguments)
data class PlayArguments(val trackId: Int, val playedAt: Instant)

fun create(server: String, authenticationData: AuthenticationData, trackId: Int, playedAt: Instant): Result<ApiPlay> {
    return "$server/api/plays".httpPost()
        .set("Accept", "application/json")
        .set("X-Secret", authenticationData.secret)
        .set("X-Device-Id", authenticationData.deviceId)
        .jsonBody(Arguments(PlayArguments(trackId, playedAt)))
        .responseObject<ApiPlay>().third
        .fold(
            { play: ApiPlay -> Result.Success(play) },
            { e: Throwable -> Result.Error(Exception("Error creating play", e)) },
        )
}

fun index(server: String, authenticationData: AuthenticationData): Sequence<Result<List<ApiPlay>>> {
    var page = 1

    fun doFetch(): Result<List<ApiPlay>>? {
        return "$server/api/plays".httpGet(listOf(Pair("page", page)))
            .set("Accept", "application/json")
            .set("X-Secret", authenticationData.secret)
            .set("X-Device-Id", authenticationData.deviceId)
            .responseObject<List<ApiPlay>>().third
            .fold(
                { p: List<ApiPlay> ->
                    if (p.isEmpty()) {
                        null
                    } else {
                        page++
                        Result.Success(p)
                    }
                },
                { e: Throwable -> Result.Error(Exception("Error getting plays", e)) }
            )
    }

    return generateSequence { doFetch() }
}
