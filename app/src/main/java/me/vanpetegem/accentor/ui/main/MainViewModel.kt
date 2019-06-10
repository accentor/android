package me.vanpetegem.accentor.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import me.vanpetegem.accentor.data.AuthenticationRepository
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class MainViewModel(private val authenticationRepository: AuthenticationRepository) : ViewModel() {
    private val _loginState = MutableLiveData<Boolean>()
    val loginState: LiveData<Boolean> = _loginState

    init {
        _loginState.value = authenticationRepository.isLoggedIn
    }

    fun logout() {
        doAsync {
            authenticationRepository.logout()
            uiThread {
                _loginState.value = false
            }
        }
    }

}