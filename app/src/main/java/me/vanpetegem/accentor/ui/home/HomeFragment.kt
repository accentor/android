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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.ui.albums.AlbumCard
import me.vanpetegem.accentor.ui.artists.ArtistCard
import me.vanpetegem.accentor.ui.player.PlayerViewModel
import me.vanpetegem.accentor.ui.util.Timer

@Composable
fun Home(navController: NavController, playerViewModel: PlayerViewModel, homeViewModel: HomeViewModel = hiltViewModel()) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            val albums by homeViewModel.recentlyReleasedAlbums.observeAsState()
            Text(stringResource(R.string.recently_released), style = MaterialTheme.typography.h5, modifier = Modifier.padding(8.dp))
            if (albums != null && albums!!.size > 0) {
                LazyRow {
                    items(albums!!.size) { i ->
                        Box(modifier = Modifier.width(192.dp)) {
                            AlbumCard(albums!![i], navController, playerViewModel)
                        }
                    }
                }
            } else {
                Text(stringResource(R.string.no_albums), modifier = Modifier.padding(horizontal = 8.dp))
            }
        }
        item {
            val albums by homeViewModel.recentlyAddedAlbums.observeAsState()
            Text(stringResource(R.string.recently_added_albums), style = MaterialTheme.typography.h5, modifier = Modifier.padding(8.dp))
            if (albums != null && albums!!.size > 0) {
                LazyRow {
                    items(albums!!.size) { i ->
                        Box(modifier = Modifier.width(192.dp)) {
                            AlbumCard(albums!![i], navController, playerViewModel)
                        }
                    }
                }
            } else {
                Text(stringResource(R.string.no_albums), modifier = Modifier.padding(horizontal = 8.dp))
            }
        }
        item {
            Timer(1000L * 60L) {
                homeViewModel.updateCurrentDay()
            }
            val currentDay by homeViewModel.currentDay.observeAsState()
            val albums by homeViewModel.albumsForDay(currentDay!!).observeAsState()
            Text(stringResource(R.string.on_this_day), style = MaterialTheme.typography.h5, modifier = Modifier.padding(8.dp))
            if (albums != null && albums!!.size > 0) {
                LazyRow {
                    items(albums!!.size) { i ->
                        Box(modifier = Modifier.width(192.dp)) {
                            AlbumCard(albums!![i], navController, playerViewModel)
                        }
                    }
                }
            } else {
                Text(stringResource(R.string.no_on_this_day), modifier = Modifier.padding(horizontal = 8.dp))
            }
        }
        item {
            val albums by homeViewModel.recentlyPlayedAlbums.observeAsState()
            Text(stringResource(R.string.recently_played_albums), style = MaterialTheme.typography.h5, modifier = Modifier.padding(8.dp))
            if (albums != null && albums!!.size > 0) {
                LazyRow {
                    items(albums!!.size) { i ->
                        Box(modifier = Modifier.width(192.dp)) {
                            AlbumCard(albums!![i], navController, playerViewModel)
                        }
                    }
                }
            } else {
                Text(stringResource(R.string.no_albums), modifier = Modifier.padding(horizontal = 8.dp))
            }
        }
        item {
            val artists by homeViewModel.recentlyAddedArtists.observeAsState()
            Text(stringResource(R.string.recently_added_artists), style = MaterialTheme.typography.h5, modifier = Modifier.padding(8.dp))
            if (artists != null && artists!!.size > 0) {
                LazyRow {
                    items(artists!!.size) { i ->
                        Box(modifier = Modifier.width(192.dp)) {
                            ArtistCard(navController, artists!![i])
                        }
                    }
                }
            } else {
                Text(stringResource(R.string.no_artists), modifier = Modifier.padding(horizontal = 8.dp))
            }
        }
        item {
            val artists by homeViewModel.recentlyPlayedArtists.observeAsState()
            Text(stringResource(R.string.recently_played_artists), style = MaterialTheme.typography.h5, modifier = Modifier.padding(8.dp))
            if (artists != null && artists!!.size > 0) {
                LazyRow {
                    items(artists!!.size) { i ->
                        Box(modifier = Modifier.width(192.dp)) {
                            ArtistCard(navController, artists!![i])
                        }
                    }
                }
            } else {
                Text(stringResource(R.string.no_artists), modifier = Modifier.padding(horizontal = 8.dp))
            }
        }
        item {
            val albums by homeViewModel.randomAlbums.observeAsState()
            Text(stringResource(R.string.random_albums), style = MaterialTheme.typography.h5, modifier = Modifier.padding(8.dp))
            if (albums != null && albums!!.size > 0) {
                LazyRow {
                    items(albums!!.size) { i ->
                        Box(modifier = Modifier.width(192.dp)) {
                            AlbumCard(albums!![i], navController, playerViewModel)
                        }
                    }
                }
            } else {
                Text(stringResource(R.string.no_albums), modifier = Modifier.padding(horizontal = 8.dp))
            }
        }
        item {
            val artists by homeViewModel.randomArtists.observeAsState()
            Text(stringResource(R.string.random_artists), style = MaterialTheme.typography.h5, modifier = Modifier.padding(8.dp))
            if (artists != null && artists!!.size > 0) {
                LazyRow {
                    items(artists!!.size) { i ->
                        Box(modifier = Modifier.width(192.dp)) {
                            ArtistCard(navController, artists!![i])
                        }
                    }
                }
            } else {
                Text(stringResource(R.string.no_artists), modifier = Modifier.padding(horizontal = 8.dp))
            }
        }
    }
}
