package me.vanpetegem.accentor.data.authentication

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
import me.vanpetegem.accentor.api.auth.create
import me.vanpetegem.accentor.api.auth.destroy
import me.vanpetegem.accentor.util.Result
import javax.inject.Inject

class AuthenticationRepository
    @Inject
    constructor(
        private val prefsSource: AuthenticationDataSource,
    ) {
        val authData: LiveData<AuthenticationData?> = prefsSource.authData
        val server: LiveData<String?> = prefsSource.server

        val isLoggedIn: LiveData<Boolean> = authData.map { it != null }

        suspend fun logout() {
            // Ignore bad data/errors for logout: if an error happens, it isn't that bad
            if (server.value != null && authData.value != null) {
                destroy(server.value!!, authData.value!!, authData.value!!.id)
            }
            withContext(Main) {
                prefsSource.setAuthData(null)
                prefsSource.setServer(null)
            }
        }

        suspend fun login(
            server: String,
            username: String,
            password: String,
        ): Result<Unit> {
            val result = create(server, username, password)

            return when (result) {
                is Result.Success -> {
                    setLoggedInUser(result.data, server)
                    Result.Success(Unit)
                }
                is Result.Error -> Result.Error(result.exception)
            }
        }

        private fun setLoggedInUser(
            authenticationData: AuthenticationData,
            server: String,
        ) {
            prefsSource.setAuthData(authenticationData)
            prefsSource.setServer(server)
        }
    }
