package me.vanpetegem.accentor.ui.albums

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.data.albums.Album
import me.vanpetegem.accentor.ui.player.PlayerViewModel

@Composable
public fun AlbumCard(album: Album, navController: NavController, playerViewModel: PlayerViewModel, hideArtist: Int? = null) {
    val scope = rememberCoroutineScope()
    Card(modifier = Modifier.padding(8.dp).clickable { navController.navigate("albums/${album.id}") }) {
        Column {
            Image(
                painter = if (album.image500 != null) {
                    rememberImagePainter(album.image500) {
                        placeholder(R.drawable.ic_album)
                    }
                } else {
                    painterResource(R.drawable.ic_album)
                },
                contentDescription = stringResource(R.string.album_image),
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                contentScale = ContentScale.Crop,
            )
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        album.title,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp),
                        style = MaterialTheme.typography.body1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        album.stringifyAlbumArtists().let {
                            if (it.isEmpty()) stringResource(R.string.various_artists) else it
                        },
                        maxLines = 1,
                        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp, end = 4.dp),
                        style = MaterialTheme.typography.body2,
                        color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                        overflow = TextOverflow.Ellipsis,
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
                                scope.launch(IO) { playerViewModel.play(album) }
                            }
                        ) {
                            Text(stringResource(R.string.play_now))
                        }
                        DropdownMenuItem(
                            onClick = {
                                expanded = false
                                scope.launch(IO) { playerViewModel.addTracksToQueue(album, maxOf(0, playerViewModel.queuePosition.value ?: 0)) }
                            }
                        ) {
                            Text(stringResource(R.string.play_next))
                        }
                        DropdownMenuItem(
                            onClick = {
                                expanded = false
                                scope.launch(IO) { playerViewModel.addTracksToQueue(album) }
                            }
                        ) {
                            Text(stringResource(R.string.play_last))
                        }
                        for (aa in album.albumArtists.sortedBy { it.order }) {
                            if (aa.artistId != hideArtist) {
                                DropdownMenuItem(
                                    onClick = {
                                        expanded = false
                                        navController.navigate("artists/${aa.artistId}")
                                    }
                                ) {
                                    Text(stringResource(R.string.go_to, aa.name))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
