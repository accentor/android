package me.vanpetegem.accentor.ui.artists

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import me.vanpetegem.accentor.ui.util.FastScrollableGrid

@Composable
fun ArtistGrid(navController: NavController, artistsViewModel: ArtistsViewModel = viewModel()) {
    val artists by artistsViewModel.allArtists.observeAsState()
    if (artists != null) {
        FastScrollableGrid(artists!!, { it.firstCharacter().uppercase() }) { artist -> ArtistCard(navController, artist) }
    }
}
