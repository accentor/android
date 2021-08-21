package me.vanpetegem.accentor.ui.albums

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ScaffoldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.ui.main.BaseToolbar
import me.vanpetegem.accentor.ui.main.MainViewModel
import me.vanpetegem.accentor.ui.main.SearchToolbar
import me.vanpetegem.accentor.ui.player.PlayerViewModel
import me.vanpetegem.accentor.ui.util.FastScrollableGrid

@Composable
fun AlbumGrid(navController: NavController, playerViewModel: PlayerViewModel, albumsViewModel: AlbumsViewModel = hiltViewModel()) {
    val albums by albumsViewModel.filteredAlbums.observeAsState()
    if (albums != null) {
        FastScrollableGrid(albums!!, { it.firstCharacter().uppercase() }) { album -> AlbumCard(album, navController, playerViewModel) }
    }
}

@Composable
fun AlbumToolbar(scaffoldState: ScaffoldState, mainViewModel: MainViewModel, albumsViewModel: AlbumsViewModel = hiltViewModel()) {
    val searching by albumsViewModel.searching.observeAsState()
    if (searching ?: false) {
        val query by albumsViewModel.query.observeAsState()
        SearchToolbar(query ?: "", { albumsViewModel.setQuery(it) }) {
            albumsViewModel.setSearching(false)
            albumsViewModel.setQuery("")
        }
    } else {
        BaseToolbar(
            scaffoldState,
            mainViewModel,
            extraActions = {
                IconButton(onClick = { albumsViewModel.setSearching(true) }) {
                    Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.search))
                }
            }
        )
    }
}
