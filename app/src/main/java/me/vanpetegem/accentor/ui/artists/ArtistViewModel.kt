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
            copy.sortWith({ t1, t2 ->
                val a1 = albums[t1.albumId]
                val a2 = albums[t2.albumId]
                if (a1 == null && a2 == null) {
                    t1.number - t2.number
                } else if (a1 == null) {
                    1
                } else if (a2 == null) {
                    -1
                } else {
                    val order = compareAlbums(a1, a2)
                    if (order == 0) {
                        t1.number - t2.number
                    } else {
                        order
                    }
                }
            })
            copy
        }
    }

    private fun compareAlbums(a1: Album, a2: Album): Int {
        var order = a1.normalizedTitle.compareTo(a2.normalizedTitle)
        order = if (order == 0) a1.release.compareTo(a2.release) else order
        order = if (order == 0) compareAlbumEditions(a1, a2) else order
        order = if (order == 0) a1.id - a2.id else order
        return order
    }

    private fun compareAlbumEditions(a1: Album, a2: Album): Int {
        if (a1.edition == null && a2.edition == null) { return 0 }
        if (a1.edition == null) { return -1 }
        if (a2.edition == null) { return 1 }
        val order = a1.edition.compareTo(a2.edition)
        return if (order == 0) a1.editionDescription!!.compareTo(a2.editionDescription!!) else order
    }
}
