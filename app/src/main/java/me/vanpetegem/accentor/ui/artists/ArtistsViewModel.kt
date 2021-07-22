package me.vanpetegem.accentor.ui.artists

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import me.vanpetegem.accentor.data.artists.Artist
import me.vanpetegem.accentor.data.artists.ArtistRepository

@HiltViewModel
class ArtistsViewModel @Inject constructor(
    application: Application,
    private val artistRepository: ArtistRepository,
) : AndroidViewModel(application) {
    val allArtists: LiveData<List<Artist>> = artistRepository.allArtists
}
