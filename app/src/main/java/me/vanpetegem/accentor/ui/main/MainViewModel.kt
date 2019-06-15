package me.vanpetegem.accentor.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.data.AccentorDatabase
import me.vanpetegem.accentor.data.albums.AlbumRepository
import me.vanpetegem.accentor.data.artists.ArtistRepository
import me.vanpetegem.accentor.data.authentication.AuthenticationDataSource
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository
import me.vanpetegem.accentor.data.users.User
import me.vanpetegem.accentor.data.users.UserRepository

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val authenticationRepository = AuthenticationRepository(AuthenticationDataSource(application))
    private val userRepository: UserRepository
    private val albumRepository: AlbumRepository
    private val artistRepository: ArtistRepository

    val currentUser: LiveData<User?>
    val loginState: LiveData<Boolean> = authenticationRepository.isLoggedIn
    val serverURL = authenticationRepository.server

    init {
        val database = AccentorDatabase.getDatabase(application)
        userRepository = UserRepository(database.userDao(), authenticationRepository)
        albumRepository = AlbumRepository(database.albumDao(), authenticationRepository)
        artistRepository = ArtistRepository(database.artistDao(), authenticationRepository)
        currentUser = userRepository.currentUser
    }

    private val _navState = MutableLiveData<NavState>()
    val navState: LiveData<NavState> = _navState


    init {
        _navState.value = NavState(R.id.nav_home, false)
    }

    fun refresh() {
        userRepository.refresh {}
        albumRepository.refresh {}
        artistRepository.refresh {}
    }

    fun logout() {
        authenticationRepository.logout()
        userRepository.clear()
        albumRepository.clear()
        artistRepository.clear()
    }

    fun navigate(item: Int) {
        _navState.value = NavState(item, false)
    }
}