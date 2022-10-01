package me.vanpetegem.accentor.ui.playlists

import android.app.Application
import android.util.SparseArray
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import me.vanpetegem.accentor.data.albums.Album
import me.vanpetegem.accentor.data.albums.AlbumRepository
import me.vanpetegem.accentor.data.artists.Artist
import me.vanpetegem.accentor.data.artists.ArtistRepository
import me.vanpetegem.accentor.data.playlists.Playlist
import me.vanpetegem.accentor.data.playlists.PlaylistRepository
import me.vanpetegem.accentor.data.tracks.Track
import me.vanpetegem.accentor.data.tracks.TrackRepository
import me.vanpetegem.accentor.data.users.User
import me.vanpetegem.accentor.data.users.UserRepository

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    application: Application,
    private val playlistRepository: PlaylistRepository,
    private val userRepository: UserRepository,
    private val albumRepository: AlbumRepository,
    private val artistRepository: ArtistRepository,
    private val trackRepository: TrackRepository,
) : AndroidViewModel(application) {
    val allUsersById: LiveData<SparseArray<User>> = userRepository.allUsersById
    val allAlbumsById: LiveData<SparseArray<Album>> = albumRepository.allAlbumsById
    val allArtistsById: LiveData<SparseArray<Artist>> = artistRepository.allArtistsById

    fun getPlaylist(id: Int): LiveData<Playlist> = map(playlistRepository.allPlaylistsById) { playlists -> playlists[id] }

    fun getTracksForPlaylist(playlist: Playlist): LiveData<SparseArray<Track>> = map(trackRepository.findByIds(playlist.itemIds)) {
        val map = SparseArray<Track>()
        it.forEach { t -> map.put(t.id, t) }
        map
    }
}
