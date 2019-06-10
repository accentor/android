package me.vanpetegem.accentor.ui.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import me.vanpetegem.accentor.data.AuthenticationRepository
import me.vanpetegem.accentor.data.NetworkAuthenticationDataSource
import me.vanpetegem.accentor.data.SharedPreferencesAuthenticationDataSource

class LoginViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(
                authenticationRepository = AuthenticationRepository(
                    networkSource = NetworkAuthenticationDataSource(),
                    prefsSource = SharedPreferencesAuthenticationDataSource(context)
                )
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
