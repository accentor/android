package me.vanpetegem.accentor.ui.albums

import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import me.vanpetegem.accentor.ui.util.FastScrollableGrid

@Composable
fun AlbumGrid(albumsViewModel: AlbumsViewModel = viewModel()) {
    val albums = albumsViewModel.allAlbums.observeAsState()
    if (albums.value != null) {
        FastScrollableGrid(albums.value!!, { it.firstCharacter().uppercase() }) { album -> AlbumCard(album) }
    }
}
