package me.vanpetegem.accentor.util

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
sealed class Result<out T> {
    data class Success<out T>(
        val data: T,
    ) : Result<T>()

    data class Error(
        val exception: Exception,
    ) : Result<Nothing>()

    override fun toString(): String =
        when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
        }
}

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
sealed class CreateResult<out T> {
    data class Success<out T>(
        val data: T,
    ) : CreateResult<T>()

    class Unprocessable : CreateResult<Nothing>()

    data class Error(
        val exception: Exception,
    ) : CreateResult<Nothing>()

    override fun toString(): String =
        when (this) {
            is Success<*> -> "Success[data=$data]"
            is Unprocessable -> "Unprocessable"
            is Error -> "Error[exception=$exception]"
        }
}
