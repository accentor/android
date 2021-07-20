package me.vanpetegem.accentor.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.ui.albums.AlbumCard
import me.vanpetegem.accentor.ui.util.Timer

@Composable
fun Home(navController: NavController, homeViewModel: HomeViewModel = viewModel()) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            val albums by homeViewModel.recentlyReleasedAlbums.observeAsState()
            if (albums != null) {
                Text(stringResource(R.string.recently_released), style = MaterialTheme.typography.h5, modifier = Modifier.padding(8.dp))
                LazyRow {
                    items(albums!!.size) { i ->
                        Box(modifier = Modifier.width(192.dp)) {
                            AlbumCard(albums!![i], navController)
                        }
                    }
                }
            }
        }
        item {
            val albums by homeViewModel.recentlyAddedAlbums.observeAsState()
            if (albums != null) {
                Text(stringResource(R.string.recently_added), style = MaterialTheme.typography.h5, modifier = Modifier.padding(8.dp))
                LazyRow {
                    items(albums!!.size) { i ->
                        Box(modifier = Modifier.width(192.dp)) {
                            AlbumCard(albums!![i], navController)
                        }
                    }
                }
            }
        }
        item {
            Timer(1000L * 60L) {
                homeViewModel.updateCurrentDay()
            }
            val currentDay by homeViewModel.currentDay.observeAsState()
            val albums by homeViewModel.albumsForDay(currentDay!!).observeAsState()
            if (albums != null) {
                Text(stringResource(R.string.on_this_day), style = MaterialTheme.typography.h5, modifier = Modifier.padding(8.dp))
                LazyRow {
                    items(albums!!.size) { i ->
                        Box(modifier = Modifier.width(192.dp)) {
                            AlbumCard(albums!![i], navController)
                        }
                    }
                }
            }
        }
        item {
            val albums by homeViewModel.randomAlbums.observeAsState()
            if (albums != null) {
                Text(stringResource(R.string.random_albums), style = MaterialTheme.typography.h5, modifier = Modifier.padding(8.dp))
                LazyRow {
                    items(albums!!.size) { i ->
                        Box(modifier = Modifier.width(192.dp)) {
                            AlbumCard(albums!![i], navController)
                        }
                    }
                }
            }
        }
    }
}
