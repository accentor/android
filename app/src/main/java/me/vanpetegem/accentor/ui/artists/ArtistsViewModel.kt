package me.vanpetegem.accentor.ui.artists

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.Transformations.switchMap
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.Normalizer
import javax.inject.Inject
import me.vanpetegem.accentor.data.artists.Artist
import me.vanpetegem.accentor.data.artists.ArtistRepository

@HiltViewModel
class ArtistsViewModel @Inject constructor(
    application: Application,
    private val artistRepository: ArtistRepository,
) : AndroidViewModel(application) {
    val allArtists: LiveData<List<Artist>> = artistRepository.allArtists

    private val _searching = MutableLiveData<Boolean>(false)
    val searching: LiveData<Boolean> = _searching

    private val _query = MutableLiveData<String>("")
    val query: LiveData<String> = _query

    val filteredArtists: LiveData<List<Artist>> = switchMap(allArtists) { artists ->
        map(query) { query ->
            if (query.equals("")) {
                artists
            } else {
                artists.filter { a -> a.normalizedName.contains(Normalizer.normalize(query, Normalizer.Form.NFKD), ignoreCase = true) }
            }
        }
    }

    fun setSearching(value: Boolean) { _searching.value = value }
    fun setQuery(value: String) { _query.value = value }
}
