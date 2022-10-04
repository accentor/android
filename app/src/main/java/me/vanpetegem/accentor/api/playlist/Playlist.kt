package me.vanpetegem.accentor.api.playlist

import com.github.kittinunf.fuel.httpGet
import me.vanpetegem.accentor.api.util.retry
import me.vanpetegem.accentor.data.authentication.AuthenticationData
import me.vanpetegem.accentor.data.playlists.ApiPlaylist
import me.vanpetegem.accentor.util.Result
import me.vanpetegem.accentor.util.responseObject

fun index(server: String, authenticationData: AuthenticationData): Sequence<Result<List<ApiPlaylist>>> {
    var page = 1

    fun doFetch(): Result<List<ApiPlaylist>>? {
        return retry(5) {
            "$server/api/playlists".httpGet(listOf(Pair("page", page)))
                .set("Accept", "application/json")
                .set("X-Secret", authenticationData.secret)
                .set("X-Device-Id", authenticationData.deviceId)
                .responseObject<List<ApiPlaylist>>().third
                .fold(
                    { a: List<ApiPlaylist> ->
                        if (a.isEmpty()) {
                            null
                        } else {
                            page++
                            Result.Success(a)
                        }
                    },
                    { e: Throwable -> Result.Error(Exception("Error getting playlists", e)) },
                )
        }
    }

    return generateSequence { doFetch() }
}
