package me.vanpetegem.accentor.ui.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ContentAlpha
import androidx.compose.material.DismissValue
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.Text
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.data.albums.Album
import me.vanpetegem.accentor.data.tracks.Track
import me.vanpetegem.accentor.media.MediaSessionConnection
import me.vanpetegem.accentor.util.formatTrackLength

@Composable
fun Queue(mediaSessionConnection: MediaSessionConnection = viewModel()) {
    val state = rememberLazyListState()
    val queue by mediaSessionConnection.queue.observeAsState()
    LazyColumn(state = state) {
        items(queue?.size ?: 0, key = { Pair(it, queue!![it].second?.id) }) { i ->
            QueueItem(mediaSessionConnection, i, queue!![i])
        }
    }
}

@Composable
fun QueueItem(mediaSessionConnection: MediaSessionConnection, index: Int, item: Triple<Boolean, Track?, Album?>) {
    if (index != 0) {
        Divider()
    }
    val scope = rememberCoroutineScope()
    val dismissState = rememberDismissState {
        if (it == DismissValue.DismissedToEnd || it == DismissValue.DismissedToStart) {
            scope.launch(IO) { mediaSessionConnection.removeFromQueue(index) }
            true
        } else {
            false
        }
    }
    SwipeToDismiss(
        state = dismissState,
        background = { Surface() {} },
        dismissContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable { scope.launch(IO) { mediaSessionConnection.skipTo(index) } }
            ) {
                if (item.first) {
                    Icon(
                        painterResource(R.drawable.ic_play),
                        contentDescription = stringResource(R.string.now_playing),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        item.second?.title ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.subtitle1,
                    )
                    Text(
                        item.second?.stringifyTrackArtists() ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.subtitle2,
                        color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                    )
                }
                Text(
                    item.second?.length.formatTrackLength(),
                    maxLines = 1,
                    style = MaterialTheme.typography.body2,
                    color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                )
            }
        },
    )
}
