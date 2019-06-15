package me.vanpetegem.accentor.data.authentication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import me.vanpetegem.accentor.api.auth.create
import me.vanpetegem.accentor.api.auth.destroy
import me.vanpetegem.accentor.util.Result
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class AuthenticationRepository(
    private val prefsSource: AuthenticationDataSource
) {

    private val _authData = MutableLiveData<AuthenticationData?>()
    val authData: LiveData<AuthenticationData?> = _authData

    val isLoggedIn: LiveData<Boolean> = Transformations.map(authData) { x -> x != null }

    private val _server = MutableLiveData<String?>()
    val server: LiveData<String?> = _server

    init {
        _authData.value = prefsSource.authData
        _server.value = prefsSource.server
    }

    fun logout() {
        doAsync {
            // Ignore bad data/errors for logout: if an error happens, it isn't that bad
            if (server.value != null && authData.value != null) {
                destroy(server.value!!, authData.value!!, authData.value!!.id)
            }
            uiThread {
                _authData.value = null
                _server.value = null
                prefsSource.authData = null
                prefsSource.server = null
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
        _authData.value = authenticationData
        _server.value = server
        prefsSource.authData = authenticationData
        prefsSource.server = server
    }
}
