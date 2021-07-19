package me.vanpetegem.accentor.ui.player

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media2.common.SessionPlayer
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.media.MediaSessionConnection
import me.vanpetegem.accentor.ui.util.Timer
import me.vanpetegem.accentor.util.formatTrackLength

@Composable
fun Controls(mediaSessionConnection: MediaSessionConnection = viewModel()) {
    val scope = rememberCoroutineScope()
    val isPlaying by mediaSessionConnection.playing.observeAsState()
    val shuffleMode by mediaSessionConnection.shuffleMode.observeAsState()
    val repeatMode by mediaSessionConnection.repeatMode.observeAsState()
    Column {
        Row(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 12.dp)) {
            when (repeatMode) {
                SessionPlayer.REPEAT_MODE_ALL -> {
                    IconButton(onClick = { scope.launch(IO) { mediaSessionConnection.setRepeatMode(SessionPlayer.REPEAT_MODE_ONE) } }) {
                        Icon(
                            painterResource(R.drawable.ic_repeat_all),
                            contentDescription = stringResource(R.string.repeat_all),
                            modifier = Modifier.height(32.dp).aspectRatio(1f),
                            tint = MaterialTheme.colors.secondary.copy(alpha = LocalContentAlpha.current),
                        )
                    }
                }
                SessionPlayer.REPEAT_MODE_ONE -> {
                    IconButton(onClick = { scope.launch(IO) { mediaSessionConnection.setRepeatMode(SessionPlayer.REPEAT_MODE_NONE) } }) {
                        Icon(
                            painterResource(R.drawable.ic_repeat_one),
                            contentDescription = stringResource(R.string.repeat_one),
                            modifier = Modifier.height(32.dp).aspectRatio(1f),
                            tint = MaterialTheme.colors.secondary.copy(alpha = LocalContentAlpha.current),
                        )
                    }
                }
                else -> {
                    IconButton(onClick = { scope.launch(IO) { mediaSessionConnection.setRepeatMode(SessionPlayer.REPEAT_MODE_ALL) } }) {
                        Icon(
                            painterResource(R.drawable.ic_repeat_off),
                            contentDescription = stringResource(R.string.repeat_off),
                            modifier = Modifier.height(32.dp).aspectRatio(1f),
                        )
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            IconButton(
                onClick = {
                    scope.launch(IO) { mediaSessionConnection.previous() }
                },
            ) {
                Icon(
                    painterResource(R.drawable.ic_previous),
                    contentDescription = stringResource(R.string.previous),
                    modifier = Modifier.height(56.dp).aspectRatio(1f),
                )
            }
            if (isPlaying ?: false) {
                IconButton(
                    onClick = {
                        scope.launch(IO) { mediaSessionConnection.pause() }
                    },
                ) {
                    Icon(
                        painterResource(R.drawable.ic_pause), contentDescription = stringResource(R.string.pause),

                        modifier = Modifier.height(56.dp).aspectRatio(1f),
                    )
                }
            } else {
                IconButton(
                    onClick = {
                        scope.launch(IO) { mediaSessionConnection.play() }
                    },
                ) {
                    Icon(
                        painterResource(R.drawable.ic_play), contentDescription = stringResource(R.string.play),

                        modifier = Modifier.height(56.dp).aspectRatio(1f),
                    )
                }
            }
            IconButton(
                onClick = {
                    scope.launch(IO) { mediaSessionConnection.next() }
                },
            ) {
                Icon(
                    painterResource(R.drawable.ic_next), contentDescription = stringResource(R.string.next),

                    modifier = Modifier.height(56.dp).aspectRatio(1f),
                )
            }
            Spacer(Modifier.weight(1f))
            when (shuffleMode) {
                SessionPlayer.SHUFFLE_MODE_ALL -> {
                    IconButton(onClick = { scope.launch(IO) { mediaSessionConnection.setShuffleMode(SessionPlayer.SHUFFLE_MODE_NONE) } }) {
                        Icon(
                            painterResource(R.drawable.ic_shuffle_all),
                            contentDescription = stringResource(R.string.shuffle_all),
                            modifier = Modifier.height(32.dp).aspectRatio(1f),
                            tint = MaterialTheme.colors.secondary.copy(alpha = LocalContentAlpha.current),
                        )
                    }
                }
                else -> {
                    IconButton(onClick = { scope.launch(IO) { mediaSessionConnection.setShuffleMode(SessionPlayer.SHUFFLE_MODE_ALL) } }) {
                        Icon(
                            painterResource(R.drawable.ic_shuffle_none),
                            contentDescription = stringResource(R.string.shuffle_none),
                            modifier = Modifier.height(32.dp).aspectRatio(1f),
                        )
                    }
                }
            }
        }
        Row(modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            val buffering by mediaSessionConnection.buffering.observeAsState(false)
            val currentPosition by mediaSessionConnection.currentPosition.observeAsState()
            var seekPosition by remember { mutableStateOf<Int?>(null) }
            val currentTrack by mediaSessionConnection.currentTrack.observeAsState()
            val trackLength = currentTrack?.length ?: 1
            Timer { mediaSessionConnection.updateCurrentPosition() }
            Text(if (seekPosition != null) seekPosition.formatTrackLength() else currentPosition.formatTrackLength())
            Slider(
                seekPosition?.toFloat() ?: (currentPosition?.toFloat() ?: 0f),
                onValueChange = { seekPosition = it.toInt() },
                onValueChangeFinished = {
                    val positionCopy = seekPosition!!
                    scope.launch(IO) { mediaSessionConnection.seekTo(positionCopy) }
                    seekPosition = null
                },
                enabled = !buffering,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                valueRange = 0f..(trackLength.toFloat()),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colors.secondary,
                    activeTrackColor = MaterialTheme.colors.secondary,
                ),
            )
            Text(currentTrack?.length.formatTrackLength())
        }
    }
}
