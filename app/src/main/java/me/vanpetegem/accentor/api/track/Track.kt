package me.vanpetegem.accentor.api.track

import com.github.kittinunf.fuel.httpGet
import me.vanpetegem.accentor.data.authentication.AuthenticationData
import me.vanpetegem.accentor.data.tracks.Track
import me.vanpetegem.accentor.util.Result
import me.vanpetegem.accentor.util.responseObject

fun index(server: String, authenticationData: AuthenticationData): Result<List<Track>> {
    var page = 1
    val results = ArrayList<Track>()

    fun doFetch(): Result<List<Track>> {
        return "$server/api/tracks".httpGet(listOf(Pair("page", page)))
            .set("Accept", "application/json")
            .set("X-Secret", authenticationData.secret)
            .set("X-Device-Id", authenticationData.deviceId)
            .responseObject<List<Track>>().third
            .fold(
                { a: List<Track> ->
                    if (a.isEmpty()) {
                        Result.Success(results)
                    } else {
                        results.addAll(a)
                        page++
                        doFetch()
                    }
                },
                { e: Throwable -> Result.Error(Exception("Error getting tracks", e)) }
            )
    }

    return doFetch()
}
