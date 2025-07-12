package me.vanpetegem.accentor.api.auth

import android.os.Build
import com.github.kittinunf.fuel.httpDelete
import com.github.kittinunf.fuel.httpPost
import me.vanpetegem.accentor.data.authentication.AuthenticationData
import me.vanpetegem.accentor.util.Result
import me.vanpetegem.accentor.util.jsonBody
import me.vanpetegem.accentor.util.responseObject
import me.vanpetegem.accentor.version

class AuthToken(
    val user_agent: String,
)

class Credentials(
    val name: String,
    val password: String,
    val auth_token: AuthToken,
)

fun create(
    server: String,
    username: String,
    password: String,
): Result<AuthenticationData> =
    "$server/api/auth_tokens"
        .httpPost()
        .set("Accept", "application/json")
        .jsonBody(
            Credentials(
                username,
                password,
                AuthToken("Accentor $version on Android ${Build.VERSION.RELEASE} (${Build.DEVICE})"),
            ),
        ).responseObject<AuthenticationData>()
        .third
        .fold(
            { user: AuthenticationData -> Result.Success(user) },
            { e: Throwable -> Result.Error(Exception("Error logging in", e)) },
        )

fun destroy(
    server: String,
    authenticationData: AuthenticationData,
    id: Int,
): Result<Unit> =
    "$server/api/auth_tokens/$id"
        .httpDelete()
        .set("Accept", "application/json")
        .set("X-Secret", authenticationData.secret)
        .set("X-Device-Id", authenticationData.deviceId)
        .response()
        .third
        .fold(
            { Result.Success(Unit) },
            { e: Throwable -> Result.Error(Exception("Error logging out", e)) },
        )
