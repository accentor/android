package me.vanpetegem.accentor.ui.albums

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import me.vanpetegem.accentor.data.AccentorDatabase
import me.vanpetegem.accentor.data.albums.Album
import me.vanpetegem.accentor.data.albums.AlbumRepository
import me.vanpetegem.accentor.data.authentication.AuthenticationDataSource
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository

class AlbumsViewModel(application: Application) : AndroidViewModel(application) {
    private val authenticationRepository = AuthenticationRepository(AuthenticationDataSource(application))
    private val albumRepository: AlbumRepository

    val allAlbums: LiveData<List<Album>>

    init {
        val database = AccentorDatabase.getDatabase(application)
        albumRepository = AlbumRepository(database.albumDao(), authenticationRepository)
        allAlbums = albumRepository.allAlbums
    }
}
