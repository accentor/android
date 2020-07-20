package me.vanpetegem.accentor.ui.albums

import android.app.Application
import android.os.Parcelable
import android.util.SparseArray
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
    private val _scrollState = MutableLiveData<Parcelable>()
    val scrollState: LiveData<Parcelable> = _scrollState

    init {
        val database = AccentorDatabase.getDatabase(application)
        albumRepository = AlbumRepository(database.albumDao(), authenticationRepository)
        trackRepository = TrackRepository(database.trackDao(), authenticationRepository)
        allAlbums = albumRepository.allAlbums
    }

    fun saveScrollState(state: Parcelable) {
        _scrollState.value = state
    }
}
