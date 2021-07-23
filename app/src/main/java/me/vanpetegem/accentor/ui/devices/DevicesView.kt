package me.vanpetegem.accentor.ui.devices

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.devices.Device
import me.vanpetegem.accentor.ui.artists.ArtistCard
import me.vanpetegem.accentor.ui.util.FastScrollableGrid

@Composable
fun Devices(devices: SnapshotStateList<Device>) {
    FastScrollableGrid(devices, { it.firstCharacter().uppercase() }) { DeviceCard(it) }
}

@Composable
fun DeviceCard(device: Device) {
    Card(
        modifier = Modifier.padding(8.dp),
    ) {
        Column {
            Text(
                device.displayString(),
                maxLines = 1,
                modifier = Modifier.padding(4.dp),
                style = MaterialTheme.typography.subtitle1,
            )
            Text(
                device.type()
            )
        }
    }
}
