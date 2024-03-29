package me.vanpetegem.accentor.ui.tracks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.data.tracks.Track
import me.vanpetegem.accentor.ui.player.PlayerViewModel

@Composable
fun TrackRow(
    track: Track,
    navController: NavController,
    playerViewModel: PlayerViewModel,
    hideAlbum: Boolean = false,
    hideArtist: Int? = null,
) {
    val scope = rememberCoroutineScope()
    Row(
        modifier =
            Modifier.fillMaxWidth().padding(8.dp).clickable {
                scope.launch(IO) { playerViewModel.play(track) }
            },
    ) {
        Column(modifier = Modifier.weight(1f)) {
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
                color = LocalContentColor.current,
                fontWeight = FontWeight.Normal,
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
                        scope.launch(IO) { playerViewModel.addTrackToQueue(track, maxOf(0, playerViewModel.queuePosition.value ?: 0)) }
                    },
                    text = { Text(stringResource(R.string.play_next)) },
                )
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        scope.launch(IO) { playerViewModel.addTrackToQueue(track) }
                    },
                    text = { Text(stringResource(R.string.play_last)) },
                )
                if (!hideAlbum) {
                    DropdownMenuItem(
                        onClick = {
                            expanded = false
                            navController.navigate("albums/${track.albumId}")
                        },
                        text = { Text(stringResource(R.string.go_to_album)) },
                    )
                }
                val used = HashSet<Pair<Int, String>>()
                for (ta in track.trackArtists.sortedBy { ta -> ta.order }) {
                    if (ta.artistId != hideArtist && !used.contains(Pair(ta.artistId, ta.name))) {
                        used.add(Pair(ta.artistId, ta.name))
                        DropdownMenuItem(
                            onClick = {
                                expanded = false
                                navController.navigate("artists/${ta.artistId}")
                            },
                            text = { Text(stringResource(R.string.go_to, ta.name)) },
                        )
                    }
                }
            }
        }
    }
    HorizontalDivider()
}
