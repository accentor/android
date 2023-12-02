package me.vanpetegem.accentor.api.util

import me.vanpetegem.accentor.util.Result

fun <T> retry(
    n: Int,
    block: () -> Result<T>?,
): Result<T>? {
    var tries = 0
    var result: Result<T>?
    do {
        if (tries > 0) {
            Thread.sleep((2 shl tries) * 1000L)
        }
        result = block()
        tries++
    } while (tries < n && result is Result.Error)
    return result
}
