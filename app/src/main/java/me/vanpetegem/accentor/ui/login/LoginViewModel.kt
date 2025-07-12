package me.vanpetegem.accentor.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository
import me.vanpetegem.accentor.util.Result
import java.net.URI
import java.net.URISyntaxException
import javax.inject.Inject

@HiltViewModel
class LoginViewModel
    @Inject
    constructor(
        application: Application,
        private val repository: AuthenticationRepository,
    ) : AndroidViewModel(application) {
        private val _loginFormState = MutableLiveData<LoginFormState>()
        val loginFormState: LiveData<LoginFormState> = _loginFormState

        private val _loading = MutableLiveData<Boolean>()
        val loading: LiveData<Boolean> = _loading

        suspend fun login(
            server: String,
            username: String,
            password: String,
        ): LoginResult {
            _loading.postValue(true)
            val result = repository.login(server, username, password)
            _loading.postValue(false)
            return when (result) {
                is Result.Success -> LoginResult()
                is Result.Error -> {
                    LoginResult(error = R.string.login_failed)
                }
            }
        }

        fun loginDataChanged(
            server: String,
            username: String,
            password: String,
        ) {
            if (!isServerValid(server)) {
                _loginFormState.value = LoginFormState(serverError = R.string.invalid_server)
            } else if (server.equals("") || username.equals("") || password.equals("")) {
                _loginFormState.value = LoginFormState()
            } else {
                _loginFormState.value = LoginFormState(isDataValid = true)
            }
        }

        private fun isServerValid(server: String): Boolean =
            try {
                URI(server)
                server.startsWith("http", ignoreCase = true)
            } catch (e: URISyntaxException) {
                false
            }
    }
