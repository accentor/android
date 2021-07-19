package me.vanpetegem.accentor.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.vanpetegem.accentor.data.AccentorDatabase
import me.vanpetegem.accentor.data.albums.AlbumRepository
import me.vanpetegem.accentor.data.artists.ArtistRepository
import me.vanpetegem.accentor.data.authentication.AuthenticationDataSource
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository
import me.vanpetegem.accentor.data.codecconversions.CodecConversionRepository
import me.vanpetegem.accentor.data.tracks.TrackRepository
import me.vanpetegem.accentor.data.users.User
import me.vanpetegem.accentor.data.users.UserRepository

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val authenticationRepository = AuthenticationRepository(AuthenticationDataSource(application))
    private val userRepository: UserRepository
    private val albumRepository: AlbumRepository
    private val artistRepository: ArtistRepository
    private val trackRepository: TrackRepository
    private val codecConversionRepository: CodecConversionRepository

    private val refreshing = MutableLiveData<Int>()
    val isRefreshing: LiveData<Boolean> = map(refreshing) { if (it != null) it > 0 else false }

    val currentUser: LiveData<User?>
    val loginState: LiveData<Boolean> = authenticationRepository.isLoggedIn

    init {
        val database = AccentorDatabase.getDatabase(application)
        userRepository = UserRepository(database.userDao(), authenticationRepository)
        albumRepository = AlbumRepository(database.albumDao(), authenticationRepository)
        artistRepository = ArtistRepository(database.artistDao(), authenticationRepository)
        trackRepository = TrackRepository(database.trackDao(), authenticationRepository)
        codecConversionRepository = CodecConversionRepository(database.codecConversionDao(), authenticationRepository)
        currentUser = userRepository.currentUser
        refreshing.value = 0
    }

    fun refresh() {
        refreshing.value?.let { refreshing.value = it + 3 }
        viewModelScope.launch(IO) {
            codecConversionRepository.refresh {
                withContext(Main) { refreshing.value?.let { refreshing.value = it - 1 } }
            }
            userRepository.refresh {
                withContext(Main) { refreshing.value?.let { refreshing.value = it - 1 } }
            }
            trackRepository.refresh {
                withContext(Main) { refreshing.value?.let { refreshing.value = it - 1 } }
            }
        }

        refreshing.value?.let { refreshing.value = it + 2 }
        viewModelScope.launch(IO) {
            artistRepository.refresh {
                withContext(Main) { refreshing.value?.let { refreshing.value = it - 1 } }
            }
            albumRepository.refresh {
                withContext(Main) { refreshing.value?.let { refreshing.value = it - 1 } }
            }
        }
    }

    fun logout() {
        viewModelScope.launch(IO) { userRepository.clear() }
        viewModelScope.launch(IO) { albumRepository.clear() }
        viewModelScope.launch(IO) { artistRepository.clear() }
        viewModelScope.launch(IO) { trackRepository.clear() }
        viewModelScope.launch(IO) { codecConversionRepository.clear() }
        viewModelScope.launch(IO) { authenticationRepository.logout() }
    }
}
