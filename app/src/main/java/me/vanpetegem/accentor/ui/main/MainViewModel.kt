package me.vanpetegem.accentor.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.vanpetegem.accentor.data.albums.AlbumRepository
import me.vanpetegem.accentor.data.artists.ArtistRepository
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository
import me.vanpetegem.accentor.data.codecconversions.CodecConversionRepository
import me.vanpetegem.accentor.data.playlists.PlaylistRepository
import me.vanpetegem.accentor.data.plays.PlayRepository
import me.vanpetegem.accentor.data.preferences.PreferencesDataSource
import me.vanpetegem.accentor.data.tracks.TrackRepository
import me.vanpetegem.accentor.data.users.UserRepository
import me.vanpetegem.accentor.ui.util.Event
import me.vanpetegem.accentor.util.Result
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        application: Application,
        private val authenticationRepository: AuthenticationRepository,
        private val userRepository: UserRepository,
        private val albumRepository: AlbumRepository,
        private val artistRepository: ArtistRepository,
        private val trackRepository: TrackRepository,
        private val codecConversionRepository: CodecConversionRepository,
        private val playlistRepository: PlaylistRepository,
        private val playRepository: PlayRepository,
        private val preferencesDataSource: PreferencesDataSource,
    ) : AndroidViewModel(application) {
        private val refreshing = MutableLiveData(0)
        val isRefreshing: LiveData<Boolean> = refreshing.map { if (it != null) it > 0 else false }
        private var errorSinceLastRefresh: Boolean = false

        private val _latestError = MutableLiveData<Event<String>?>(null)
        val latestError: LiveData<Event<String>?> = _latestError

        val loginState: LiveData<Boolean> = authenticationRepository.isLoggedIn

        fun refresh() {
            if ((refreshing.value ?: 0) > 0) return

            errorSinceLastRefresh = false

            refreshing.value?.let { refreshing.value = it + 1 }
            viewModelScope.launch(IO) {
                codecConversionRepository.refresh { decrementRefresh(it) }
            }

            refreshing.value?.let { refreshing.value = it + 1 }
            viewModelScope.launch(IO) {
                userRepository.refresh { decrementRefresh(it) }
            }

            refreshing.value?.let { refreshing.value = it + 1 }
            viewModelScope.launch(IO) {
                trackRepository.refresh { decrementRefresh(it) }
            }

            refreshing.value?.let { refreshing.value = it + 1 }
            viewModelScope.launch(IO) {
                artistRepository.refresh { decrementRefresh(it) }
            }

            refreshing.value?.let { refreshing.value = it + 1 }
            viewModelScope.launch(IO) {
                albumRepository.refresh { decrementRefresh(it) }
            }

            refreshing.value?.let { refreshing.value = it + 1 }
            viewModelScope.launch(IO) {
                playRepository.refresh { decrementRefresh(it) }
            }

            refreshing.value?.let { refreshing.value = it + 1 }
            viewModelScope.launch(IO) {
                playlistRepository.refresh { decrementRefresh(it) }
            }
        }

        suspend fun decrementRefresh(result: Result<Unit>) {
            withContext(Main) {
                refreshing.value?.let { refreshing.value = it - 1 }
                if (result is Result.Error) {
                    errorSinceLastRefresh = true
                    _latestError.value = Event(result.exception.message!!)
                }

                if (refreshing.value == 0 && !errorSinceLastRefresh) {
                    withContext(IO) { preferencesDataSource.setLastSyncFinished(Instant.now()) }
                }
            }
        }

        fun logout() {
            viewModelScope.launch(IO) { userRepository.clear() }
            viewModelScope.launch(IO) { albumRepository.clear() }
            viewModelScope.launch(IO) { artistRepository.clear() }
            viewModelScope.launch(IO) { trackRepository.clear() }
            viewModelScope.launch(IO) { playRepository.clear() }
            viewModelScope.launch(IO) { playlistRepository.clear() }
            viewModelScope.launch(IO) { codecConversionRepository.clear() }
            viewModelScope.launch(IO) { authenticationRepository.logout() }
        }
    }
