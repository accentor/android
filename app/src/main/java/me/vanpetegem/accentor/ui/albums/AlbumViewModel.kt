package me.vanpetegem.accentor.ui.albums

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import me.vanpetegem.accentor.data.AccentorDatabase
import me.vanpetegem.accentor.data.albums.Album
import me.vanpetegem.accentor.data.albums.AlbumRepository
import me.vanpetegem.accentor.data.authentication.AuthenticationDataSource
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository
import me.vanpetegem.accentor.data.tracks.Track
import me.vanpetegem.accentor.data.tracks.TrackRepository

class AlbumViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AccentorDatabase.getDatabase(application)
    private val authenticationRepository = AuthenticationRepository(AuthenticationDataSource(application))
    private val albumRepository = AlbumRepository(database.albumDao(), authenticationRepository)
    private val trackRepository = TrackRepository(database.trackDao(), authenticationRepository)

    fun getAlbum(id: Int): LiveData<Album> = map(albumRepository.allAlbumsById) { albums ->
        albums[id]
    }

    fun tracksForAlbum(album: Album): LiveData<List<Track>> = map(trackRepository.findByAlbum(album)) { tracks ->
        val copy = tracks.toMutableList()
        copy.sortWith({ t1, t2 -> t1.number.compareTo(t2.number) })
        copy
    }
}
