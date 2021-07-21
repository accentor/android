package me.vanpetegem.accentor.ui.artists

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.ui.albums.AlbumCard
import me.vanpetegem.accentor.ui.tracks.TrackRow

@Composable
fun ArtistView(id: Int, navController: NavController, artistViewModel: ArtistViewModel = viewModel()) {
    val artistState by artistViewModel.getArtist(id).observeAsState()
    if (artistState != null) {
        val artist = artistState!!
        val albums by artistViewModel.albumsForArtist(artist).observeAsState()
        val tracks by artistViewModel.tracksForArtist(artist).observeAsState()
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = if (artist.image500 != null) {
                            rememberImagePainter(artist.image500) {
                                placeholder(R.drawable.ic_artist)
                                transformations(CircleCropTransformation())
                            }
                        } else {
                            painterResource(R.drawable.ic_artist)
                        },
                        contentDescription = stringResource(R.string.artist_image),
                        modifier = Modifier.width(80.dp).aspectRatio(1f),
                    )
                    Text(artist.name, style = MaterialTheme.typography.h4, modifier = Modifier.padding(start = 8.dp))
                }
            }
            item {
                if (albums != null && albums!!.size > 0) {
                    Text(stringResource(R.string.albums), style = MaterialTheme.typography.h5, modifier = Modifier.padding(8.dp))
                    LazyRow {
                        items(albums!!.size) { i ->
                            Box(modifier = Modifier.width(192.dp)) {
                                AlbumCard(albums!![i], navController, hideArtist = id)
                            }
                        }
                    }
                }
            }
            if (tracks != null && tracks!!.size > 0) {
                item {
                    Text(stringResource(R.string.tracks), style = MaterialTheme.typography.h5, modifier = Modifier.padding(8.dp))
                }
                items(tracks!!.size) { i -> TrackRow(tracks!![i], navController, hideArtist = id) }
            }
        }
    }
}
