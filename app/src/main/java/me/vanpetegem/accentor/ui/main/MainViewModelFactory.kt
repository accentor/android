package me.vanpetegem.accentor.ui.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import me.vanpetegem.accentor.data.AuthenticationRepository
import me.vanpetegem.accentor.data.NetworkAuthenticationDataSource
import me.vanpetegem.accentor.data.SharedPreferencesAuthenticationDataSource

class MainViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(
                authenticationRepository = AuthenticationRepository(
                    networkSource = NetworkAuthenticationDataSource(),
                    prefsSource = SharedPreferencesAuthenticationDataSource(context)
                )
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
