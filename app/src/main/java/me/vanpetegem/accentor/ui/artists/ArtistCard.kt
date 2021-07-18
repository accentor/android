package me.vanpetegem.accentor.ui.artists

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.data.artists.Artist

@Composable
public fun ArtistCard(artist: Artist) {
    Card(
        modifier = Modifier.padding(8.dp),
    ) {
        Column {
            Image(
                painter = if (artist.image500 != null) {
                    rememberImagePainter(artist.image500) {
                        placeholder(R.drawable.ic_artist)
                    }
                } else {
                    painterResource(R.drawable.ic_artist)
                },
                contentDescription = stringResource(R.string.artist_image),
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                contentScale = ContentScale.Crop,
            )
            Text(
                artist.name,
                maxLines = 1,
                modifier = Modifier.padding(4.dp),
                style = MaterialTheme.typography.subtitle1,
            )
        }
    }
}
