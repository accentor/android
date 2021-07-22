package me.vanpetegem.accentor.ui.albums

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import me.vanpetegem.accentor.data.albums.Album
import me.vanpetegem.accentor.data.albums.AlbumRepository

@HiltViewModel
class AlbumsViewModel @Inject constructor(
    application: Application,
    private val albumRepository: AlbumRepository,
) : AndroidViewModel(application) {
    val allAlbums: LiveData<List<Album>> = albumRepository.allAlbums
}
