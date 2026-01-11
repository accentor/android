package me.vanpetegem.accentor.ui.albums

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.ui.player.PlayerViewModel
import me.vanpetegem.accentor.ui.tracks.TrackRow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AlbumView(
    id: Int,
    navController: NavController,
    playerViewModel: PlayerViewModel,
    albumViewModel: AlbumViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()
    val albumState by albumViewModel.getAlbum(id).observeAsState()
    if (albumState != null) {
        val album = albumState!!
        val tracks by albumViewModel.tracksForAlbum(album).observeAsState()
        val tracksCount = tracks?.size ?: 0
        val lengthMinutes = tracks?.let { albumViewModel.sumTrackLengths(it) } ?: 0
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(bottom = 8.dp)) {
                    AsyncImage(
                        model = album.image500,
                        fallback = painterResource(R.drawable.ic_album),
                        placeholder = painterResource(R.drawable.ic_album),
                        contentDescription = stringResource(R.string.album_image),
                        modifier = Modifier.width(128.dp).aspectRatio(1f),
                    )
                    Column {
                        Text(
                            if (album.editionDescription == null) album.title else "${album.title} (${album.editionDescription})",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(start = 8.dp),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            album.stringifyAlbumArtists().let { it.ifEmpty { stringResource(R.string.various_artists) } },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(start = 8.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            if (album.edition == null) album.release.format() else "${album.release.format()} (${album.edition.format()})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(start = 8.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            "${pluralStringResource(
                                R.plurals.album_tracks_count,
                                tracksCount,
                                tracksCount,
                            )}, ${pluralStringResource(R.plurals.album_length_minutes, lengthMinutes, lengthMinutes)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(start = 8.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Row(modifier = Modifier.padding(8.dp)) {
                            IconButton(onClick = { scope.launch(IO) { playerViewModel.play(album) } }) {
                                Icon(painterResource(R.drawable.ic_play), contentDescription = stringResource(R.string.play_now))
                            }
                            IconButton(onClick = { scope.launch(IO) { playerViewModel.addTracksToQueue(album) } }) {
                                Icon(painterResource(R.drawable.ic_queue_add), contentDescription = stringResource(R.string.play_last))
                            }
                        }
                    }
                }
            }
            if (tracks != null && tracks!!.isNotEmpty()) {
                items(tracks!!.size) { i -> TrackRow(tracks!![i], navController, playerViewModel, hideAlbum = true) }
            }
        }
    }
}

@Composable
fun AlbumViewDropdown(
    id: Int,
    navController: NavController,
    dismiss: (() -> Unit),
    albumViewModel: AlbumViewModel = hiltViewModel(),
) {
    val albumState by albumViewModel.getAlbum(id).observeAsState()
    if (albumState != null) {
        val album = albumState!!
        for (aa in album.albumArtists.sortedBy { it.order }) {
            DropdownMenuItem(
                onClick = {
                    dismiss()
                    navController.navigate("artists/${aa.artistId}")
                },
                text = { Text(stringResource(R.string.go_to, aa.name)) },
            )
        }
    }
}

fun LocalDate.format(): String = format(DateTimeFormatter.ISO_LOCAL_DATE)
