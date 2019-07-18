package me.vanpetegem.accentor.ui.albums

import android.app.Application
import android.util.SparseArray
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import me.vanpetegem.accentor.data.AccentorDatabase
import me.vanpetegem.accentor.data.albums.Album
import me.vanpetegem.accentor.data.albums.AlbumRepository
import me.vanpetegem.accentor.data.authentication.AuthenticationDataSource
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository
import me.vanpetegem.accentor.data.tracks.Track
import me.vanpetegem.accentor.data.tracks.TrackRepository

class AlbumsViewModel(application: Application) : AndroidViewModel(application) {
    private val authenticationRepository = AuthenticationRepository(AuthenticationDataSource(application))
    private val albumRepository: AlbumRepository
    private val trackRepository: TrackRepository

    val allAlbums: LiveData<List<Album>>
    val tracksByAlbumId: LiveData<SparseArray<MutableList<Track>>>

    init {
        val database = AccentorDatabase.getDatabase(application)
        albumRepository = AlbumRepository(database.albumDao(), authenticationRepository)
        trackRepository = TrackRepository(database.trackDao(), authenticationRepository)
        allAlbums = albumRepository.allAlbums
        tracksByAlbumId = trackRepository.allTracksByAlbumId
    }
}
