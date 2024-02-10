package me.vanpetegem.accentor.ui.playlists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.data.playlists.Playlist
import me.vanpetegem.accentor.data.playlists.PlaylistType
import me.vanpetegem.accentor.data.users.User
import me.vanpetegem.accentor.ui.main.BaseToolbar
import me.vanpetegem.accentor.ui.main.MainViewModel
import me.vanpetegem.accentor.ui.main.SearchToolbar
import me.vanpetegem.accentor.ui.player.PlayerViewModel

@Composable
fun PlaylistList(
    navController: NavController,
    playerViewModel: PlayerViewModel,
    playlistsViewModel: PlaylistsViewModel = hiltViewModel(),
) {
    val playlists by playlistsViewModel.filteredPlaylists.observeAsState()
    val users by playlistsViewModel.allUsersById.observeAsState()
    val state = rememberLazyListState()
    LazyColumn(state = state) {
        items(playlists?.size ?: 0, key = { Pair(it, playlists!![it].id) }) { i ->
            PlaylistListItem(navController, playerViewModel, i, playlists!![i], users!!.get(playlists!![i].userId))
        }
    }
}

@Composable
fun PlaylistToolbar(
    drawerState: DrawerState,
    mainViewModel: MainViewModel,
    playlistsViewModel: PlaylistsViewModel = hiltViewModel(),
) {
    val searching by playlistsViewModel.searching.observeAsState()
    if (searching ?: false) {
        val query by playlistsViewModel.query.observeAsState()
        SearchToolbar(query ?: "", { playlistsViewModel.setQuery(it) }) {
            playlistsViewModel.setSearching(false)
            playlistsViewModel.setQuery("")
        }
    } else {
        BaseToolbar(
            drawerState,
            mainViewModel,
            extraActions = {
                IconButton(onClick = { playlistsViewModel.setSearching(true) }) {
                    Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.search))
                }
            },
        )
    }
}

@Composable
fun PlaylistListItem(
    navController: NavController,
    playerViewModel: PlayerViewModel,
    index: Int,
    playlist: Playlist,
    user: User?,
) {
    if (index != 0) {
        HorizontalDivider()
    }
    val itemInfo =
        pluralStringResource(
            when (playlist.playlistType) {
                PlaylistType.ALBUM -> R.plurals.playlist_albums
                PlaylistType.ARTIST -> R.plurals.playlist_artists
                PlaylistType.TRACK -> R.plurals.playlist_tracks
            },
            playlist.itemIds.size,
            playlist.itemIds.size,
        )
    Row(
        modifier = Modifier.padding(8.dp).clickable { navController.navigate("playlists/${playlist.id}") },
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                playlist.name,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                (user?.name ?: "") + " · " + itemInfo,
                style = MaterialTheme.typography.titleSmall,
                color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
            )
        }
    }
}
