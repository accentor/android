package me.vanpetegem.accentor.api

import com.github.kittinunf.fuel.gson.jsonBody
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpDelete
import com.github.kittinunf.fuel.httpPost
import me.vanpetegem.accentor.data.model.AuthenticationData
import me.vanpetegem.accentor.util.Result

class AuthToken(val user_agent: String)
class Credentials(val name: String, val password: String, val auth_token: AuthToken)

fun create(server: String, username: String, password: String): Result<AuthenticationData> {
    "$server/api/auth_tokens".httpPost()
        .set("Accept", "application/json")
        .jsonBody(Credentials(username, password, AuthToken("Accentor on Android")))
        .responseObject<AuthenticationData>().third
        .fold(
            { user: AuthenticationData -> return Result.Success(user) },
            { e: Throwable -> return Result.Error(Exception("Error logging in", e)) }
        )
}

fun destroy(server: String, authenticationData: AuthenticationData, id: Int): Result<Unit> {
    "$server/api/auth_tokens/$id".httpDelete()
        .set("Accept", "application/json")
        .set("X-Secret", authenticationData.secret)
        .set("X-Device-Id", authenticationData.device_id)
        .response().third
        .fold(
            { return Result.Success(Unit) },
            { e: Throwable -> return Result.Error(Exception("Error logging out", e)) }
        )
}