package me.vanpetegem.accentor.api.album

import com.github.kittinunf.fuel.httpGet
import me.vanpetegem.accentor.data.albums.ApiAlbum
import me.vanpetegem.accentor.data.authentication.AuthenticationData
import me.vanpetegem.accentor.util.Result
import me.vanpetegem.accentor.util.responseObject

fun index(server: String, authenticationData: AuthenticationData): Sequence<Result<List<ApiAlbum>>> {
    var page = 1

    fun doFetch(): Result<List<ApiAlbum>>? {
        return "$server/api/albums".httpGet(listOf(Pair("page", page)))
            .set("Accept", "application/json")
            .set("X-Secret", authenticationData.secret)
            .set("X-Device-Id", authenticationData.deviceId)
            .responseObject<List<ApiAlbum>>().third
            .fold(
                { a: List<ApiAlbum> ->
                    if (a.isEmpty()) {
                        null
                    } else {
                        page++
                        Result.Success(a)
                    }
                },
                { e: Throwable -> Result.Error(Exception("Error getting albums", e)) }
            )
    }

    return generateSequence { doFetch() }
}
