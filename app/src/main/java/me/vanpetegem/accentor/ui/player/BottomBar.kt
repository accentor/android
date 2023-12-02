package me.vanpetegem.accentor.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import me.vanpetegem.accentor.R

@Composable
fun ControlBar(playerViewModel: PlayerViewModel = viewModel()) {
    val scope = rememberCoroutineScope()
    val currentTrack by playerViewModel.currentTrack.observeAsState()
    val currentAlbum by playerViewModel.currentAlbum.observeAsState()
    val isPlaying by playerViewModel.playing.observeAsState()
    Surface(modifier = Modifier.height(64.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = currentAlbum?.image500,
                placeholder = painterResource(R.drawable.ic_album),
                fallback = painterResource(R.drawable.ic_album),
                contentDescription = stringResource(R.string.album_cover_of_current_track),
                modifier = Modifier.fillMaxHeight().aspectRatio(1f).background(MaterialTheme.colorScheme.surface),
                contentScale = ContentScale.Crop,
            )
            Column(modifier = Modifier.padding(start = 8.dp).weight(1f)) {
                Text(
                    currentTrack?.title ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    currentTrack?.stringifyTrackArtists() ?: "",
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(
                onClick = {
                    scope.launch(IO) { playerViewModel.previous() }
                },
            ) {
                Icon(painterResource(R.drawable.ic_previous), contentDescription = stringResource(R.string.previous))
            }
            if (isPlaying ?: false) {
                IconButton(
                    onClick = {
                        scope.launch(IO) { playerViewModel.pause() }
                    },
                ) {
                    Icon(painterResource(R.drawable.ic_pause), contentDescription = stringResource(R.string.pause))
                }
            } else {
                IconButton(
                    onClick = {
                        scope.launch(IO) { playerViewModel.play() }
                    },
                ) {
                    Icon(painterResource(R.drawable.ic_play), contentDescription = stringResource(R.string.play))
                }
            }
            IconButton(
                onClick = {
                    scope.launch(IO) { playerViewModel.next() }
                },
            ) {
                Icon(painterResource(R.drawable.ic_next), contentDescription = stringResource(R.string.next))
            }
        }
    }
}
