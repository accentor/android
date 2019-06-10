package me.vanpetegem.accentor.ui.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.data.AuthenticationRepository
import me.vanpetegem.accentor.util.Result
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.URI
import java.net.URISyntaxException

class LoginViewModel(private val authenticationRepository: AuthenticationRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(server: String, username: String, password: String) {
        // can be launched in a separate asynchronous job
        doAsync {
            val result = authenticationRepository.login(server, username, password)

            uiThread {
                if (result is Result.Success) {
                    _loginResult.value = LoginResult()
                } else if (result is Result.Error) {
                    Log.e("LOGIN", "login failed", result.exception)
                    _loginResult.value = LoginResult(error = R.string.login_failed)
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
