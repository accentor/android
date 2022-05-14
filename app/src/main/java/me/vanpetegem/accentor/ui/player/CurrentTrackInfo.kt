package me.vanpetegem.accentor.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import me.vanpetegem.accentor.R

@Composable
fun CurrentTrackInfo(playerViewModel: PlayerViewModel = viewModel()) {
    val currentTrack by playerViewModel.currentTrack.observeAsState()
    val currentAlbum by playerViewModel.currentAlbum.observeAsState()
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        AsyncImage(
            model = currentAlbum?.image500,
            placeholder = painterResource(R.drawable.ic_album),
            fallback = painterResource(R.drawable.ic_album),
            contentDescription = stringResource(R.string.album_cover_of_current_track),
            modifier = Modifier.weight(1f).fillMaxWidth().background(MaterialTheme.colors.surface),
            contentScale = ContentScale.Fit,
            alignment = Alignment.TopCenter,
        )
        Text(
            currentTrack?.title ?: "",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp),
            style = MaterialTheme.typography.h6,
        )
        Text(
            currentTrack?.stringifyTrackArtists() ?: "",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp),
            style = MaterialTheme.typography.subtitle1,
            color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
        )
        Text(
            currentAlbum?.title ?: "",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp),
            style = MaterialTheme.typography.subtitle2,
            color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
        )
    }
}
