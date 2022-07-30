package me.vanpetegem.accentor.ui.player

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import me.vanpetegem.accentor.R

@Composable
fun ToolBar(
    showQueueButton: Boolean,
    playerViewModel: PlayerViewModel = viewModel(),
    closePlayer: (() -> Unit)
) {
    val scope = rememberCoroutineScope()
    val queuePosStr by playerViewModel.queuePosStr.observeAsState()
    TopAppBar(
        title = {
            Column {
                Text(
                    stringResource(R.string.now_playing),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.h6,
                )
                Text(
                    queuePosStr ?: "0/0",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.subtitle1,
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = closePlayer) {
                Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.close_player))
            }
        },
        actions = {
            if (showQueueButton) {
                IconButton(onClick = { playerViewModel.toggleQueue() }) {
                    Icon(painterResource(R.drawable.ic_play_queue), contentDescription = stringResource(R.string.toggle_queue))
                }
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
                            scope.launch(IO) { playerViewModel.clearQueue() }
                        }
                    ) {
                        Text(stringResource(R.string.clear_queue))
                    }
                }
            }
        }
    )
}
