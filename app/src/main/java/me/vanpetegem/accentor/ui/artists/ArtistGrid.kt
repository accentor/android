package me.vanpetegem.accentor.ui.artists

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import me.vanpetegem.accentor.ui.util.FastScrollableGrid

@Composable
fun ArtistGrid(
    navController: NavController,
    artistsViewModel: ArtistsViewModel = hiltViewModel(),
) {
    val artists by artistsViewModel.filteredArtists.observeAsState()
    if (artists != null) {
        FastScrollableGrid(artists!!, { it.firstCharacter().uppercase() }) { artist -> ArtistCard(navController, artist) }
    }
}

@Composable
fun ArtistToolbar(
    drawerState: DrawerState,
    mainViewModel: MainViewModel,
    artistsViewModel: ArtistsViewModel = hiltViewModel(),
) {
    val searching by artistsViewModel.searching.observeAsState()
    if (searching ?: false) {
        val query by artistsViewModel.query.observeAsState()
        SearchToolbar(query ?: "", { artistsViewModel.setQuery(it) }) {
            artistsViewModel.setSearching(false)
            artistsViewModel.setQuery("")
        }
    } else {
        BaseToolbar(
            drawerState,
            mainViewModel,
            extraActions = {
                IconButton(onClick = { artistsViewModel.setSearching(true) }) {
                    Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.search))
                }
            },
        )
    }
}
