package me.vanpetegem.accentor.ui.artists

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.data.tracks.Track
import me.vanpetegem.accentor.media.MediaSessionConnection
import me.vanpetegem.accentor.ui.albums.AlbumCard

@Composable
fun ArtistView(id: Int, artistViewModel: ArtistViewModel = viewModel()) {
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
                                AlbumCard(albums!![i])
                            }
                        }
                    }
                }
            }
            if (tracks != null && tracks!!.size > 0) {
                item {
                    Text(stringResource(R.string.tracks), style = MaterialTheme.typography.h5, modifier = Modifier.padding(8.dp))
                }
                items(tracks!!.size) { i -> TrackRow(tracks!![i]) }
            }
        }
    }
}

@Composable
fun TrackRow(track: Track, mediaSessionConnection: MediaSessionConnection = viewModel()) {
    val scope = rememberCoroutineScope()
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp).clickable {
            scope.launch(IO) { mediaSessionConnection.play(track) }
        }
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                track.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.subtitle1,
            )
            Text(
                track.stringifyTrackArtists(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.subtitle2,
                color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
            )
        }
        var expanded by remember { mutableStateOf(false) }
        Box(modifier = Modifier.height(40.dp).aspectRatio(1f).wrapContentSize(Alignment.TopStart)) {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.open_menu))
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        scope.launch(IO) { mediaSessionConnection.addTrackToQueue(track, maxOf(0, mediaSessionConnection.queuePosition.value ?: 0)) }
                    }
                ) {
                    Text(stringResource(R.string.play_next))
                }
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        scope.launch(IO) { mediaSessionConnection.addTrackToQueue(track) }
                    }
                ) {
                    Text(stringResource(R.string.play_last))
                }
            }
        }
    }
    Divider()
}
