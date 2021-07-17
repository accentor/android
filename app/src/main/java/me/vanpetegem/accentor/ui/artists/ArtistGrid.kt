package me.vanpetegem.accentor.ui.artists

import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import me.vanpetegem.accentor.ui.util.FastScrollableGrid

@Composable
fun ArtistGrid(artistsViewModel: ArtistsViewModel = viewModel()) {
    val artists = artistsViewModel.allArtists.observeAsState()
    if (artists.value != null) {
        FastScrollableGrid(artists.value!!, { it.firstCharacter().uppercase() }) { artist -> ArtistCard(artist) }
    }
}
