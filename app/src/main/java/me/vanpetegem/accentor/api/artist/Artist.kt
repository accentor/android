package me.vanpetegem.accentor.api.artist

import com.github.kittinunf.fuel.httpGet
import me.vanpetegem.accentor.data.artists.Artist
import me.vanpetegem.accentor.data.authentication.AuthenticationData
import me.vanpetegem.accentor.util.Result
import me.vanpetegem.accentor.util.responseObject
import java.util.*
import kotlin.collections.ArrayList

fun index(server: String, authenticationData: AuthenticationData): Result<List<Artist>> {

    var page = 1
    val results = ArrayList<Artist>()

    fun doFetch(): Result<List<Artist>> {
        "$server/api/artists".httpGet(Arrays.asList(Pair("page", page)))
            .set("Accept", "application/json")
            .set("X-Secret", authenticationData.secret)
            .set("X-Device-Id", authenticationData.deviceId)
            .responseObject<List<Artist>>().third
            .fold(
                { a: List<Artist> ->
                    return if (a.isEmpty()) {
                        Result.Success(results)
                    } else {
                        results.addAll(a)
                        page++
                        doFetch()
                    }
                },
                { e: Throwable -> return Result.Error(Exception("Error getting artists", e)) }
            )
    }

    return doFetch()
}