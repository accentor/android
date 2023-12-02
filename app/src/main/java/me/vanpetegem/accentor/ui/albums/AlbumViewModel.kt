package me.vanpetegem.accentor.ui.albums

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import dagger.hilt.android.lifecycle.HiltViewModel
import me.vanpetegem.accentor.data.albums.Album
import me.vanpetegem.accentor.data.albums.AlbumRepository
import me.vanpetegem.accentor.data.tracks.Track
import me.vanpetegem.accentor.data.tracks.TrackRepository
import javax.inject.Inject

@HiltViewModel
class AlbumViewModel
    @Inject
    constructor(
        application: Application,
        private val albumRepository: AlbumRepository,
        private val trackRepository: TrackRepository,
    ) : AndroidViewModel(application) {
        fun getAlbum(id: Int): LiveData<Album> =
            albumRepository.allAlbumsById.map { albums ->
                albums[id]
            }

        fun tracksForAlbum(album: Album): LiveData<List<Track>> =
            trackRepository.findByAlbum(album).map { tracks ->
                val copy = tracks.toMutableList()
                copy.sortWith({ t1, t2 -> t1.number.compareTo(t2.number) })
                copy
            }
    }
