package me.vanpetegem.accentor.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import me.vanpetegem.accentor.data.AccentorDatabase
import me.vanpetegem.accentor.data.albums.AlbumRepository
import me.vanpetegem.accentor.data.authentication.AuthenticationDataSource
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val authenticationRepository = AuthenticationRepository(AuthenticationDataSource(application))
    private val albumRepository: AlbumRepository

    init {
        val database = AccentorDatabase.getDatabase(application)
        albumRepository = AlbumRepository(database.albumDao(), authenticationRepository)
    }
}
