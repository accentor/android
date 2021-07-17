package me.vanpetegem.accentor.ui.albums

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.ui.AccentorTheme
import me.vanpetegem.accentor.ui.util.FastScrollableGrid

class AlbumsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_albums, container, false).apply {
            findViewById<ComposeView>(R.id.compose_view).setContent {
                AccentorTheme {
                    AlbumGrid()
                }
            }
        }
    }
}

@Composable
fun AlbumGrid(albumsViewModel: AlbumsViewModel = viewModel()) {
    val albums = albumsViewModel.allAlbums.observeAsState()
    if (albums.value != null) {
        FastScrollableGrid(albums.value!!, { it.firstCharacter().uppercase() }) { album -> AlbumCard(album) }
    }
}
