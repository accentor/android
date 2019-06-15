package me.vanpetegem.accentor.ui.artists

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import me.vanpetegem.accentor.data.AccentorDatabase
import me.vanpetegem.accentor.data.artists.Artist
import me.vanpetegem.accentor.data.artists.ArtistRepository
import me.vanpetegem.accentor.data.authentication.AuthenticationDataSource
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository

class ArtistsViewModel(application: Application) : AndroidViewModel(application) {
    private val authenticationRepository = AuthenticationRepository(AuthenticationDataSource(application))
    private val artistRepository: ArtistRepository

    val allArtists: LiveData<List<Artist>>

    init {
        val database = AccentorDatabase.getDatabase(application)
        artistRepository = ArtistRepository(database.artistDao(), authenticationRepository)
        allArtists = artistRepository.allArtists
    }
}
