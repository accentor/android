package me.vanpetegem.accentor.ui.albums

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.media.MediaSessionConnection
import me.vanpetegem.accentor.ui.tracks.TrackRow

@Composable
fun AlbumView(id: Int, albumViewModel: AlbumViewModel = viewModel(), mediaSessionConnection: MediaSessionConnection = viewModel()) {
    val scope = rememberCoroutineScope()
    val albumState by albumViewModel.getAlbum(id).observeAsState()
    if (albumState != null) {
        val album = albumState!!
        val tracks by albumViewModel.tracksForAlbum(album).observeAsState()
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(bottom = 8.dp)) {
                    Image(
                        painter = if (album.image500 != null) {
                            rememberImagePainter(album.image500) {
                                placeholder(R.drawable.ic_album)
                            }
                        } else {
                            painterResource(R.drawable.ic_album)
                        },
                        contentDescription = stringResource(R.string.artist_image),
                        modifier = Modifier.width(128.dp).aspectRatio(1f),
                    )
                    Column {
                        Text(
                            if (album.editionDescription == null) album.title else "${album.title} (${album.editionDescription})",
                            style = MaterialTheme.typography.h5,
                            modifier = Modifier.padding(start = 8.dp),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            album.stringifyAlbumArtists(),
                            style = MaterialTheme.typography.subtitle1,
                            color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                            modifier = Modifier.padding(start = 8.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            if (album.edition == null) album.release.format() else "${album.release.format()} (${album.edition.format()})",
                            style = MaterialTheme.typography.subtitle2,
                            color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                            modifier = Modifier.padding(start = 8.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Row(modifier = Modifier.padding(8.dp)) {
                            IconButton(onClick = { scope.launch(IO) { mediaSessionConnection.play(album) } }) {
                                Icon(painterResource(R.drawable.ic_play), contentDescription = stringResource(R.string.play_now))
                            }
                            IconButton(onClick = { scope.launch(IO) { mediaSessionConnection.addTracksToQueue(album) } }) {
                                Icon(painterResource(R.drawable.ic_queue_add), contentDescription = stringResource(R.string.play_last))
                            }
                        }
                    }
                }
            }
            if (tracks != null && tracks!!.size > 0) {
                items(tracks!!.size) { i -> TrackRow(tracks!![i]) }
            }
        }
    }
}

fun LocalDate.format(): String = format(DateTimeFormatter.ISO_LOCAL_DATE)
