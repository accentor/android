package me.vanpetegem.accentor.ui.artists

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.Transformations.switchMap
import me.vanpetegem.accentor.data.AccentorDatabase
import me.vanpetegem.accentor.data.albums.Album
import me.vanpetegem.accentor.data.albums.AlbumRepository
import me.vanpetegem.accentor.data.artists.Artist
import me.vanpetegem.accentor.data.artists.ArtistRepository
import me.vanpetegem.accentor.data.authentication.AuthenticationDataSource
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository
import me.vanpetegem.accentor.data.tracks.Track
import me.vanpetegem.accentor.data.tracks.TrackRepository

class ArtistViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AccentorDatabase.getDatabase(application)
    private val authenticationRepository = AuthenticationRepository(AuthenticationDataSource(application))
    private val artistRepository = ArtistRepository(database.artistDao(), authenticationRepository)
    private val albumRepository = AlbumRepository(database.albumDao(), authenticationRepository)
    private val trackRepository = TrackRepository(database.trackDao(), authenticationRepository)

    fun getArtist(id: Int): LiveData<Artist> = map(artistRepository.allArtistsById) { artists ->
        artists[id]
    }

    fun albumsForArtist(artist: Artist): LiveData<List<Album>> = map(albumRepository.albumsByReleased) { albums ->
        val result = albums.filter { it.albumArtists.any { it.artistId == artist.id } }.toMutableList()
        result.reverse()
        result
    }

    fun tracksForArtist(artist: Artist): LiveData<List<Track>> = switchMap(trackRepository.findByArtist(artist)) { tracks ->
        map(albumRepository.allAlbumsById) { albums ->
            val copy = tracks.toMutableList()
            copy.sortWith({ t1, t2 -> t1.compareTo(t2, albums) })
            copy
        }
    }
}
