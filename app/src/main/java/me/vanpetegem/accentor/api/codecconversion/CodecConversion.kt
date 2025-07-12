package me.vanpetegem.accentor.api.codecconversion

import com.github.kittinunf.fuel.httpGet
import me.vanpetegem.accentor.api.util.retry
import me.vanpetegem.accentor.data.authentication.AuthenticationData
import me.vanpetegem.accentor.data.codecconversions.ApiCodecConversion
import me.vanpetegem.accentor.util.Result
import me.vanpetegem.accentor.util.responseObject

fun index(
    server: String,
    authenticationData: AuthenticationData,
): Sequence<Result<List<ApiCodecConversion>>> {
    var page = 1

    fun doFetch(): Result<List<ApiCodecConversion>>? =
        retry(5) {
            "$server/api/codec_conversions"
                .httpGet(listOf(Pair("page", page)))
                .set("Accept", "application/json")
                .set("X-Secret", authenticationData.secret)
                .set("X-Device-Id", authenticationData.deviceId)
                .responseObject<List<ApiCodecConversion>>()
                .third
                .fold(
                    { c: List<ApiCodecConversion> ->
                        if (c.isEmpty()) {
                            null
                        } else {
                            page++
                            Result.Success(c)
                        }
                    },
                    { e: Throwable -> Result.Error(Exception("Error getting codec conversions", e)) },
                )
        }

    return generateSequence { doFetch() }
}
