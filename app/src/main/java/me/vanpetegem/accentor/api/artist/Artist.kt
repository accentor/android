package me.vanpetegem.accentor.api.artist

import com.github.kittinunf.fuel.httpGet
import me.vanpetegem.accentor.data.artists.Artist
import me.vanpetegem.accentor.data.authentication.AuthenticationData
import me.vanpetegem.accentor.util.Result
import me.vanpetegem.accentor.util.responseObject

fun index(server: String, authenticationData: AuthenticationData): Result<List<Artist>> {
    var page = 1
    val results = ArrayList<Artist>()

    fun doFetch(): Result<List<Artist>> {
        return "$server/api/artists".httpGet(listOf(Pair("page", page)))
            .set("Accept", "application/json")
            .set("X-Secret", authenticationData.secret)
            .set("X-Device-Id", authenticationData.deviceId)
            .responseObject<List<Artist>>().third
            .fold(
                { a: List<Artist> ->
                    if (a.isEmpty()) {
                        Result.Success(results)
                    } else {
                        results.addAll(a)
                        page++
                        doFetch()
                    }
                },
                { e: Throwable -> Result.Error(Exception("Error getting artists", e)) }
            )
    }

    return doFetch()
}
