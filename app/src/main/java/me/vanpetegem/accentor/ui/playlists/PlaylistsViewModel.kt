package me.vanpetegem.accentor.ui.playlists

import android.app.Application
import android.util.SparseArray
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import dagger.hilt.android.lifecycle.HiltViewModel
import me.vanpetegem.accentor.data.playlists.Playlist
import me.vanpetegem.accentor.data.playlists.PlaylistRepository
import me.vanpetegem.accentor.data.users.User
import me.vanpetegem.accentor.data.users.UserRepository
import java.text.Normalizer
import javax.inject.Inject

@HiltViewModel
class PlaylistsViewModel
    @Inject
    constructor(
        application: Application,
        playlistRepository: PlaylistRepository,
        userRepository: UserRepository,
    ) : AndroidViewModel(application) {
        val allPlaylists: LiveData<List<Playlist>> = playlistRepository.allPlaylists
        val allUsersById: LiveData<SparseArray<User>> = userRepository.allUsersById

        private val _searching = MutableLiveData(false)
        val searching: LiveData<Boolean> = _searching

        private val _query = MutableLiveData("")
        val query: LiveData<String> = _query

        val filteredPlaylists: LiveData<List<Playlist>> =
            allPlaylists.switchMap { playlists ->
                query.map { query ->
                    if (query == "") {
                        playlists
                    } else {
                        playlists.filter { p -> p.name.contains(Normalizer.normalize(query, Normalizer.Form.NFKD), ignoreCase = true) }
                    }
                }
            }

        fun setSearching(value: Boolean) {
            _searching.value = value
        }

        fun setQuery(value: String) {
            _query.value = value
        }
    }
