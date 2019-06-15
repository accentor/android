package me.vanpetegem.accentor.api.user

import com.github.kittinunf.fuel.httpGet
import me.vanpetegem.accentor.data.authentication.AuthenticationData
import me.vanpetegem.accentor.data.users.User
import me.vanpetegem.accentor.util.Result
import me.vanpetegem.accentor.util.responseObject
import java.util.Arrays.asList

fun index(server: String, authenticationData: AuthenticationData): Result<List<User>> {

    var page = 1
    val results = ArrayList<User>()

    fun doFetch(): Result<List<User>> {
        "$server/api/users".httpGet(asList(Pair("page", page)))
            .set("Accept", "application/json")
            .set("X-Secret", authenticationData.secret)
            .set("X-Device-Id", authenticationData.deviceId)
            .responseObject<List<User>>().third
            .fold(
                { u: List<User> ->
                    return if (u.isEmpty()) {
                        Result.Success(results)
                    } else {
                        results.addAll(u)
                        page++
                        doFetch()
                    }
                },
                { e: Throwable -> return Result.Error(Exception("Error getting users", e)) }
            )
    }

    return doFetch()
}