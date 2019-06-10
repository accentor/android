package me.vanpetegem.accentor.data

import me.vanpetegem.accentor.data.model.AuthenticationData
import me.vanpetegem.accentor.util.Result

class AuthenticationRepository(
    private val networkSource: NetworkAuthenticationDataSource,
    private val prefsSource: SharedPreferencesAuthenticationDataSource
) {

    private var authData: AuthenticationData? = null

    val isLoggedIn: Boolean
        get() = authData != null

    val server: String?
        get() = prefsSource.server

    init {
        authData = prefsSource.authData
    }

    fun logout() {
        // Ignore bad data/errors for logout: if an error happens, it isn't that bad
        if (server != null && authData != null) {
            networkSource.logout(server!!, authData!!)
        }
        authData = null
        prefsSource.authData = null
        prefsSource.server = null
    }

    fun login(server: String, username: String, password: String): Result<AuthenticationData> {
        val result = networkSource.login(server, username, password)

        if (result is Result.Success) {
            setLoggedInUser(result.data, server)
        }

        return result
    }

    private fun setLoggedInUser(authenticationData: AuthenticationData, server: String) {
        this.authData = authenticationData
        prefsSource.authData = this.authData
        prefsSource.server = server
    }
}
