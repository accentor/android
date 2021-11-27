package me.vanpetegem.accentor.api.user

import com.github.kittinunf.fuel.httpGet
import me.vanpetegem.accentor.data.authentication.AuthenticationData
import me.vanpetegem.accentor.data.users.ApiUser
import me.vanpetegem.accentor.util.Result
import me.vanpetegem.accentor.util.responseObject

fun index(server: String, authenticationData: AuthenticationData): Sequence<Result<List<ApiUser>>> {
    var page = 1

    fun doFetch(): Result<List<ApiUser>>? {
        return "$server/api/users".httpGet(listOf(Pair("page", page)))
            .set("Accept", "application/json")
            .set("X-Secret", authenticationData.secret)
            .set("X-Device-Id", authenticationData.deviceId)
            .responseObject<List<ApiUser>>().third
            .fold(
                { u: List<ApiUser> ->
                    if (u.isEmpty()) {
                        null
                    } else {
                        page++
                        Result.Success(u)
                    }
                },
                { e: Throwable -> Result.Error(Exception("Error getting users", e)) }
            )
    }

    return generateSequence { doFetch() }
}
