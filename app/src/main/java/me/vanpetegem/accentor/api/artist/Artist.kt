package me.vanpetegem.accentor.api.artist

import com.github.kittinunf.fuel.httpGet
import me.vanpetegem.accentor.api.util.retry
import me.vanpetegem.accentor.data.artists.ApiArtist
import me.vanpetegem.accentor.data.authentication.AuthenticationData
import me.vanpetegem.accentor.util.Result
import me.vanpetegem.accentor.util.responseObject

fun index(
    server: String,
    authenticationData: AuthenticationData,
): Sequence<Result<List<ApiArtist>>> {
    var page = 1

    fun doFetch(): Result<List<ApiArtist>>? =
        retry(5) {
            "$server/api/artists"
                .httpGet(listOf(Pair("page", page)))
                .set("Accept", "application/json")
                .set("Authorization", "Token ${authenticationData.token}")
                .responseObject<List<ApiArtist>>()
                .third
                .fold(
                    { a: List<ApiArtist> ->
                        if (a.isEmpty()) {
                            null
                        } else {
                            page++
                            Result.Success(a)
                        }
                    },
                    { e: Throwable -> Result.Error(Exception("Error getting artists", e)) },
                )
        }

    return generateSequence { doFetch() }
}
