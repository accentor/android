package me.vanpetegem.accentor.data.authentication

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import me.vanpetegem.accentor.api.auth.create
import me.vanpetegem.accentor.api.auth.destroy
import me.vanpetegem.accentor.util.Result
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class AuthenticationRepository(
    private val prefsSource: AuthenticationDataSource
) {

    val authData: LiveData<AuthenticationData> = prefsSource.authData
    val server: LiveData<String> = prefsSource.server

    val isLoggedIn: LiveData<Boolean> = Transformations.map(authData) {
        it != null
    }

    fun logout() {
        doAsync {
            // Ignore bad data/errors for logout: if an error happens, it isn't that bad
            if (server.value != null && authData.value != null) {
                destroy(server.value!!, authData.value!!, authData.value!!.id)
            }
            uiThread {
                prefsSource.setAuthData(null)
                prefsSource.setServer(null)
            }
        }
    }

    fun login(server: String, username: String, password: String, handler: (Result<Unit>) -> Unit) {
        doAsync {
            val result = create(server, username, password)

            uiThread {
                handler(
                    when (result) {
                        is Result.Success -> {
                            setLoggedInUser(result.data, server)
                            Result.Success(Unit)
                        }
                        is Result.Error -> Result.Error(result.exception)
                    }
                )
            }
        }
    }

    private fun setLoggedInUser(authenticationData: AuthenticationData, server: String) {
        prefsSource.setAuthData(authenticationData)
        prefsSource.setServer(server)
    }
}
