package me.vanpetegem.accentor.ui.devices

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.devices.Device
import me.vanpetegem.accentor.ui.util.FastScrollableGrid
import org.fourthline.cling.model.types.UDN

@Composable
fun Devices(devices: SnapshotStateMap<UDN, Device>) {
    FastScrollableGrid(devices.values.sortedBy { it.friendlyName }, { it.firstCharacter.uppercase() }) { DeviceCard(it) }
}

@Composable
fun DeviceCard(device: Device) {
    Card(
        modifier = Modifier.padding(8.dp),
    ) {
        Column {
            Image(
                painter = if (device.imageURL != null) {
                    rememberImagePainter(device.imageURL) {
                        placeholder(R.drawable.ic_artist)
                    }
                } else {
                    painterResource(R.drawable.ic_artist)
                },
                contentDescription = stringResource(R.string.device_image),
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                contentScale = ContentScale.Crop,
            )
            Text(
                device.friendlyName,
                maxLines = 1,
                modifier = Modifier.padding(4.dp),
                style = MaterialTheme.typography.subtitle1,
            )
            Text(
                device.type
            )
        }
    }
}
