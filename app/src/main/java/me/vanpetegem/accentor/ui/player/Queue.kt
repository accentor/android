package me.vanpetegem.accentor.ui.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ContentAlpha
import androidx.compose.material.DismissValue
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.data.albums.Album
import me.vanpetegem.accentor.data.tracks.Track
import me.vanpetegem.accentor.util.formatTrackLength

@Composable
fun Queue(
    navController: NavController,
    closePlayer: (() -> Unit),
    playerViewModel: PlayerViewModel = viewModel(),
) {
    val queue by playerViewModel.queue.observeAsState()
    val queuePosition by playerViewModel.queuePosition.observeAsState()
    val state = rememberLazyListState((queuePosition ?: 1) - 1)
    LazyColumn(state = state) {
        items(queue?.size ?: 0, key = { Pair(it, queue!![it].second?.id) }) { i ->
            QueueItem(playerViewModel, navController, i, queue!![i], closePlayer)
        }
    }
}

@Composable
fun QueueItem(
    playerViewModel: PlayerViewModel,
    navController: NavController,
    index: Int,
    item: Triple<Boolean, Track?, Album?>,
    closePlayer: (() -> Unit),
) {
    if (index != 0) {
        Divider()
    }
    val scope = rememberCoroutineScope()
    val dismissState =
        rememberDismissState {
            if (it == DismissValue.DismissedToEnd || it == DismissValue.DismissedToStart) {
                scope.launch(IO) { playerViewModel.removeFromQueue(index) }
                true
            } else {
                false
            }
        }
    SwipeToDismiss(
        state = dismissState,
        background = { Surface {} },
        dismissContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier
                        .padding(8.dp)
                        .clickable {
                            scope.launch(IO) {
                                playerViewModel.skipTo(index)
                                playerViewModel.play()
                            }
                        },
            ) {
                val track = item.second
                if (item.first) {
                    Icon(
                        painterResource(R.drawable.ic_play),
                        contentDescription = stringResource(R.string.now_playing),
                        modifier = Modifier.padding(end = 8.dp),
                    )
                }
                if (track != null) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            track.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            track.stringifyTrackArtists(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleSmall,
                            color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                        )
                    }
                    Text(
                        track.length.formatTrackLength(),
                        maxLines = 1,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                    )
                    Box(modifier = Modifier.height(40.dp).aspectRatio(1f).wrapContentSize(Alignment.TopStart)) {
                        var expanded by remember { mutableStateOf(false) }
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.open_menu))
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(
                                onClick = {
                                    expanded = false
                                    navController.navigate("albums/${track.albumId}")
                                    closePlayer()
                                },
                                text = { Text(stringResource(R.string.go_to_album)) },
                            )
                            for (ta in track.trackArtists.sortedBy { ta -> ta.order }) {
                                DropdownMenuItem(
                                    onClick = {
                                        expanded = false
                                        navController.navigate("artists/${ta.artistId}")
                                        closePlayer()
                                    },
                                    text = { Text(stringResource(R.string.go_to, ta.name)) },
                                )
                            }
                        }
                    }
                }
            }
        },
    )
}
