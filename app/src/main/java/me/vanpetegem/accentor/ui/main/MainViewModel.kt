package me.vanpetegem.accentor.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.vanpetegem.accentor.data.albums.AlbumRepository
import me.vanpetegem.accentor.data.artists.ArtistRepository
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository
import me.vanpetegem.accentor.data.codecconversions.CodecConversionRepository
import me.vanpetegem.accentor.data.plays.PlayRepository
import me.vanpetegem.accentor.data.tracks.TrackRepository
import me.vanpetegem.accentor.data.users.User
import me.vanpetegem.accentor.data.users.UserRepository

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val authenticationRepository: AuthenticationRepository,
    private val userRepository: UserRepository,
    private val albumRepository: AlbumRepository,
    private val artistRepository: ArtistRepository,
    private val trackRepository: TrackRepository,
    private val codecConversionRepository: CodecConversionRepository,
    private val playRepository: PlayRepository,
) : AndroidViewModel(application) {
    private val refreshing = MutableLiveData<Int>(0)
    val isRefreshing: LiveData<Boolean> = map(refreshing) { if (it != null) it > 0 else false }

    val currentUser: LiveData<User?> = userRepository.currentUser
    val loginState: LiveData<Boolean> = authenticationRepository.isLoggedIn

    fun refresh() {
        if ((refreshing.value ?: 0) > 0) return

        refreshing.value?.let { refreshing.value = it + 1 }
        viewModelScope.launch(IO) {
            codecConversionRepository.refresh {
                withContext(Main) { refreshing.value?.let { refreshing.value = it - 1 } }
            }
        }

        refreshing.value?.let { refreshing.value = it + 1 }
        viewModelScope.launch(IO) {
            userRepository.refresh {
                withContext(Main) { refreshing.value?.let { refreshing.value = it - 1 } }
            }
        }

        refreshing.value?.let { refreshing.value = it + 1 }
        viewModelScope.launch(IO) {
            trackRepository.refresh {
                withContext(Main) { refreshing.value?.let { refreshing.value = it - 1 } }
            }
        }

        refreshing.value?.let { refreshing.value = it + 1 }
        viewModelScope.launch(IO) {
            artistRepository.refresh {
                withContext(Main) { refreshing.value?.let { refreshing.value = it - 1 } }
            }
        }

        refreshing.value?.let { refreshing.value = it + 1 }
        viewModelScope.launch(IO) {
            albumRepository.refresh {
                withContext(Main) { refreshing.value?.let { refreshing.value = it - 1 } }
            }
        }

        refreshing.value?.let { refreshing.value = it + 1 }
        viewModelScope.launch(IO) {
            playRepository.refresh {
                withContext(Main) { refreshing.value?.let { refreshing.value = it - 1 } }
            }
        }
    }

    fun logout() {
        viewModelScope.launch(IO) { userRepository.clear() }
        viewModelScope.launch(IO) { albumRepository.clear() }
        viewModelScope.launch(IO) { artistRepository.clear() }
        viewModelScope.launch(IO) { trackRepository.clear() }
        viewModelScope.launch(IO) { playRepository.clear() }
        viewModelScope.launch(IO) { codecConversionRepository.clear() }
        viewModelScope.launch(IO) { authenticationRepository.logout() }
    }
}
