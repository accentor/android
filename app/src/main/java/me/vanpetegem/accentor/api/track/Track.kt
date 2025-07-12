package me.vanpetegem.accentor.api.track

import com.github.kittinunf.fuel.httpGet
import me.vanpetegem.accentor.api.util.retry
import me.vanpetegem.accentor.data.authentication.AuthenticationData
import me.vanpetegem.accentor.data.tracks.ApiTrack
import me.vanpetegem.accentor.util.Result
import me.vanpetegem.accentor.util.responseObject

fun index(
    server: String,
    authenticationData: AuthenticationData,
): Sequence<Result<List<ApiTrack>>> {
    var page = 1

    fun doFetch(): Result<List<ApiTrack>>? =
        retry(5) {
            "$server/api/tracks"
                .httpGet(listOf(Pair("page", page)))
                .set("Accept", "application/json")
                .set("X-Secret", authenticationData.secret)
                .set("X-Device-Id", authenticationData.deviceId)
                .responseObject<List<ApiTrack>>()
                .third
                .fold(
                    { a: List<ApiTrack> ->
                        if (a.isEmpty()) {
                            null
                        } else {
                            page++
                            Result.Success(a)
                        }
                    },
                    { e: Throwable -> Result.Error(Exception("Error getting tracks", e)) },
                )
        }

    return generateSequence { doFetch() }
}
