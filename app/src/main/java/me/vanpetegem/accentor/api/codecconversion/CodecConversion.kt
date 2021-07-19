package me.vanpetegem.accentor.api.codecconversion

import com.github.kittinunf.fuel.httpGet
import me.vanpetegem.accentor.data.authentication.AuthenticationData
import me.vanpetegem.accentor.data.codecconversions.CodecConversion
import me.vanpetegem.accentor.util.Result
import me.vanpetegem.accentor.util.responseObject

fun index(server: String, authenticationData: AuthenticationData): Result<List<CodecConversion>> {
    var page = 1
    val results = ArrayList<CodecConversion>()

    fun doFetch(): Result<List<CodecConversion>> {
        return "$server/api/codec_conversions".httpGet(listOf(Pair("page", page)))
            .set("Accept", "application/json")
            .set("X-Secret", authenticationData.secret)
            .set("X-Device-Id", authenticationData.deviceId)
            .responseObject<List<CodecConversion>>().third
            .fold(
                { u: List<CodecConversion> ->
                    if (u.isEmpty()) {
                        Result.Success(results)
                    } else {
                        results.addAll(u)
                        page++
                        doFetch()
                    }
                },
                { e: Throwable -> Result.Error(Exception("Error getting codec conversions", e)) }
            )
    }

    return doFetch()
}
