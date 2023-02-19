package me.vanpetegem.accentor.ui.artists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.data.artists.Artist

@Composable
public fun ArtistCard(navController: NavController, artist: Artist) {
    Card(
        modifier = Modifier.padding(8.dp).clickable { navController.navigate("artists/${artist.id}") }
    ) {
        Column {
            AsyncImage(
                model = artist.image500,
                fallback = painterResource(R.drawable.ic_artist),
                placeholder = painterResource(R.drawable.ic_artist),
                contentDescription = stringResource(R.string.artist_image),
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                contentScale = ContentScale.Crop
            )
            Text(
                artist.name,
                maxLines = 1,
                modifier = Modifier.padding(4.dp),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
