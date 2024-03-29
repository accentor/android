package me.vanpetegem.accentor.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
            modifier = Modifier.weight(1f).fillMaxWidth().background(MaterialTheme.colorScheme.surface),
            contentScale = ContentScale.Fit,
            alignment = Alignment.TopCenter,
        )
        Text(
            currentTrack?.title ?: "",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            currentTrack?.stringifyTrackArtists() ?: "",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Normal,
            color = LocalContentColor.current,
        )
        Text(
            currentAlbum?.title ?: "",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Normal,
            color = LocalContentColor.current,
        )
    }
}
