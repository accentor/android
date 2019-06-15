package me.vanpetegem.accentor.api.album

import com.github.kittinunf.fuel.httpGet
import me.vanpetegem.accentor.data.albums.Album
import me.vanpetegem.accentor.data.authentication.AuthenticationData
import me.vanpetegem.accentor.util.Result
import me.vanpetegem.accentor.util.responseObject
import java.util.*
import kotlin.collections.ArrayList

fun index(server: String, authenticationData: AuthenticationData): Result<List<Album>> {

    var page = 1
    val results = ArrayList<Album>()

    fun doFetch(): Result<List<Album>> {
        "$server/api/albums".httpGet(Arrays.asList(Pair("page", page)))
            .set("Accept", "application/json")
            .set("X-Secret", authenticationData.secret)
            .set("X-Device-Id", authenticationData.deviceId)
            .responseObject<List<Album>>().third
            .fold(
                { a: List<Album> ->
                    return if (a.isEmpty()) {
                        Result.Success(results)
                    } else {
                        results.addAll(a)
                        page++
                        doFetch()
                    }
                },
                { e: Throwable -> return Result.Error(Exception("Error getting albums", e)) }
            )
    }

    return doFetch()
}