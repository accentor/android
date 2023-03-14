package me.vanpetegem.accentor.ui.artists

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import me.vanpetegem.accentor.data.albums.Album
import me.vanpetegem.accentor.data.albums.AlbumRepository
import me.vanpetegem.accentor.data.artists.Artist
import me.vanpetegem.accentor.data.artists.ArtistRepository
import me.vanpetegem.accentor.data.tracks.Track
import me.vanpetegem.accentor.data.tracks.TrackRepository

@HiltViewModel
class ArtistViewModel @Inject constructor(
    application: Application,
    private val artistRepository: ArtistRepository,
    private val albumRepository: AlbumRepository,
    private val trackRepository: TrackRepository
) : AndroidViewModel(application) {
    fun getArtist(id: Int): LiveData<Artist> = artistRepository.allArtistsById.map { artists ->
        artists[id]
    }

    fun albumsForArtist(artist: Artist): LiveData<List<Album>> = albumRepository.albumsByReleased.map { albums ->
        val result = albums.filter { it.albumArtists.any { it.artistId == artist.id } }.toMutableList()
        result.reverse()
        result
    }

    fun tracksForArtist(artist: Artist): LiveData<List<Track>> = trackRepository.findByArtist(artist).switchMap { tracks ->
        albumRepository.allAlbumsById.map { albums ->
            val copy = tracks.toMutableList()
            copy.sortWith({ t1, t2 -> t1.compareAlphabetically(t2, albums) })
            copy
        }
    }
}
