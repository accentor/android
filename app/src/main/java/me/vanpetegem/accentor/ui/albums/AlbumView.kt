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
import androidx.compose.material.LocalContentColor
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
import coil.compose.rememberImagePainter
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.ui.tracks.TrackRow

@Composable
fun AlbumView(id: Int, albumViewModel: AlbumViewModel = viewModel()) {
    val albumState by albumViewModel.getAlbum(id).observeAsState()
    if (albumState != null) {
        val album = albumState!!
        val tracks by albumViewModel.tracksForAlbum(album).observeAsState()
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = if (album.image500 != null) {
                            rememberImagePainter(album.image500) {
                                placeholder(R.drawable.ic_album)
                            }
                        } else {
                            painterResource(R.drawable.ic_album)
                        },
                        contentDescription = stringResource(R.string.artist_image),
                        modifier = Modifier.width(192.dp).aspectRatio(1f),
                    )
                    Column {
                        Text(album.title, style = MaterialTheme.typography.h4, modifier = Modifier.padding(start = 8.dp))
                        Text(
                            album.stringifyAlbumArtists(),
                            style = MaterialTheme.typography.subtitle1,
                            color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
            if (tracks != null && tracks!!.size > 0) {
                items(tracks!!.size) { i -> TrackRow(tracks!![i]) }
            }
        }
    }
}
