package me.vanpetegem.accentor.ui.login

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.data.authentication.AuthenticationDataSource
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository
import me.vanpetegem.accentor.util.Result
import java.net.URI
import java.net.URISyntaxException

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AuthenticationRepository = AuthenticationRepository(AuthenticationDataSource(application))

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(server: String, username: String, password: String) {
        repository.login(server, username, password) { result ->
            _loginResult.value = when (result) {
                is Result.Success -> LoginResult()
                is Result.Error -> {
                    Log.e("Accentor", "login failed", result.exception)
                    LoginResult(error = R.string.login_failed)
                }
            }
        }
    }

    fun loginDataChanged(server: String) {
        if (!isServerValid(server)) {
            _loginForm.value = LoginFormState(serverError = R.string.invalid_server)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    private fun isServerValid(server: String): Boolean {
        return try {
            URI(server)
            server.startsWith("http", ignoreCase = true)
        } catch (e: URISyntaxException) {
            false
        }
    }
}
