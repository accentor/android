package me.vanpetegem.accentor.ui.playlists

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.ContentAlpha
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.data.playlists.Playlist
import me.vanpetegem.accentor.data.playlists.PlaylistType
import me.vanpetegem.accentor.ui.albums.AlbumCard
import me.vanpetegem.accentor.ui.artists.ArtistCard
import me.vanpetegem.accentor.ui.player.PlayerViewModel
import me.vanpetegem.accentor.ui.tracks.TrackRow

@Composable
fun PlaylistView(
    id: Int,
    navController: NavController,
    playerViewModel: PlayerViewModel,
    playlistViewModel: PlaylistViewModel = hiltViewModel(),
) {
    val users by playlistViewModel.allUsersById.observeAsState()
    val playlistState by playlistViewModel.getPlaylist(id).observeAsState()
    if (playlistState != null) {
        val playlist = playlistState!!
        val user = users!!.get(playlist.userId)
        val itemInfo = pluralStringResource(
            when (playlist.playlistType) {
                PlaylistType.ALBUM -> R.plurals.playlist_albums
                PlaylistType.ARTIST -> R.plurals.playlist_artists
                PlaylistType.TRACK -> R.plurals.playlist_tracks
            },
            playlist.itemIds.size, playlist.itemIds.size
        )
        Column() {
            Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Text(
                    playlist.name,
                    style = MaterialTheme.typography.headlineLarge,
                )
                Text(
                    (user?.name ?: "") + " · " + itemInfo,
                    style = MaterialTheme.typography.headlineMedium,
                    color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                )
            }
            when (playlist.playlistType) {
                PlaylistType.ALBUM -> PlaylistAlbumContent(navController, playerViewModel, playlistViewModel, playlist)
                PlaylistType.ARTIST -> PlaylistArtistContent(navController, playlistViewModel, playlist)
                PlaylistType.TRACK -> PlaylistTrackContent(navController, playerViewModel, playlistViewModel, playlist)
            }
        }
    }
}

@Composable
fun PlaylistAlbumContent(
    navController: NavController,
    playerViewModel: PlayerViewModel,
    playlistViewModel: PlaylistViewModel,
    playlist: Playlist,
) {
    val albums by playlistViewModel.allAlbumsById.observeAsState()
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    val cardsPerRow: Int = with(LocalDensity.current) { boxSize.width / 192.dp.toPx().toInt() }
    val gridState = rememberLazyGridState()
    if (albums != null) {
        LazyVerticalGrid(
            columns = if (cardsPerRow >= 2) GridCells.Adaptive(minSize = 192.dp) else GridCells.Fixed(2),
            state = gridState,
            modifier = Modifier.onGloballyPositioned { boxSize = it.size },
        ) {
            items(playlist.itemIds.size) { i -> AlbumCard(albums!![playlist.itemIds[i]], navController, playerViewModel) }
        }
    }
}

@Composable
fun PlaylistArtistContent(
    navController: NavController,
    playlistViewModel: PlaylistViewModel,
    playlist: Playlist,
) {
    val artists by playlistViewModel.allArtistsById.observeAsState()
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    val cardsPerRow: Int = with(LocalDensity.current) { boxSize.width / 192.dp.toPx().toInt() }
    val gridState = rememberLazyGridState()
    if (artists != null) {
        LazyVerticalGrid(
            columns = if (cardsPerRow >= 2) GridCells.Adaptive(minSize = 192.dp) else GridCells.Fixed(2),
            state = gridState,
            modifier = Modifier.onGloballyPositioned { boxSize = it.size },
        ) {
            items(playlist.itemIds.size) { i -> ArtistCard(navController, artists!![playlist.itemIds[i]]) }
        }
    }
}

@Composable
fun PlaylistTrackContent(
    navController: NavController,
    playerViewModel: PlayerViewModel,
    playlistViewModel: PlaylistViewModel,
    playlist: Playlist,
) {
    val tracks by playlistViewModel.getTracksForPlaylist(playlist).observeAsState()
    if (tracks != null) {
        LazyColumn() {
            items(playlist.itemIds.size) { i -> TrackRow(tracks!![playlist.itemIds[i]], navController, playerViewModel) }
        }
    }
}
