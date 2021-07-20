package me.vanpetegem.accentor.ui.albums

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import me.vanpetegem.accentor.data.AccentorDatabase
import me.vanpetegem.accentor.data.albums.Album
import me.vanpetegem.accentor.data.albums.AlbumRepository
import me.vanpetegem.accentor.data.authentication.AuthenticationDataSource
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository
import me.vanpetegem.accentor.data.tracks.TrackRepository

class AlbumsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AccentorDatabase.getDatabase(application)
    private val authenticationRepository = AuthenticationRepository(AuthenticationDataSource(application))
    private val albumRepository = AlbumRepository(database.albumDao(), authenticationRepository)
    private val trackRepository = TrackRepository(database.trackDao(), authenticationRepository)

    val allAlbums: LiveData<List<Album>> = albumRepository.allAlbums
}
