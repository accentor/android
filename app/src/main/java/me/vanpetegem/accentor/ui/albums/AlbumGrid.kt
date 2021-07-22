package me.vanpetegem.accentor.ui.albums

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import me.vanpetegem.accentor.ui.util.FastScrollableGrid

@Composable
fun AlbumGrid(navController: NavController, albumsViewModel: AlbumsViewModel = hiltViewModel()) {
    val albums by albumsViewModel.allAlbums.observeAsState()
    if (albums != null) {
        FastScrollableGrid(albums!!, { it.firstCharacter().uppercase() }) { album -> AlbumCard(album, navController) }
    }
}
