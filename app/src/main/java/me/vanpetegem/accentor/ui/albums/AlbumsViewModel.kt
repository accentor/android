package me.vanpetegem.accentor.ui.albums

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import dagger.hilt.android.lifecycle.HiltViewModel
import me.vanpetegem.accentor.data.albums.Album
import me.vanpetegem.accentor.data.albums.AlbumRepository
import java.text.Normalizer
import javax.inject.Inject

@HiltViewModel
class AlbumsViewModel
    @Inject
    constructor(
        application: Application,
        albumRepository: AlbumRepository,
    ) : AndroidViewModel(application) {
        val allAlbums: LiveData<List<Album>> = albumRepository.allAlbums

        private val _searching = MutableLiveData(false)
        val searching: LiveData<Boolean> = _searching

        private val _query = MutableLiveData("")
        val query: LiveData<String> = _query

        val filteredAlbums: LiveData<List<Album>> =
            allAlbums.switchMap { albums ->
                query.map { query ->
                    if (query == "") {
                        albums
                    } else {
                        albums.filter { a -> a.normalizedTitle.contains(Normalizer.normalize(query, Normalizer.Form.NFKD), ignoreCase = true) }
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
