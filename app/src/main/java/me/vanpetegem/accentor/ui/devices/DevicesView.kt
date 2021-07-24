package me.vanpetegem.accentor.ui.devices

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.devices.Device

@Composable
fun Devices(devicesViewModel: DevicesViewModel = hiltViewModel()) {
    val devices: List<Device>? by devicesViewModel.devices().observeAsState()
    DeviceList(devices ?: emptyList())
}

@Composable
fun DeviceList(devices: List<Device>) {
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        DeviceCard(
            name = stringResource(R.string.local_device),
            icon = R.drawable.ic_smartphone_sound,
            iconDescription = R.string.local_device_description
        )
        Spacer(Modifier.size(8.dp))
        Text(
            stringResource(R.string.devices_available),
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.h5
        )
        devices.forEach { device ->
            DeviceCard(
                name = device.friendlyName,
                icon = R.drawable.ic_menu_devices
            )
        }
    }
}

@Composable
fun DeviceCard(
    name: String,
    @StringRes
    iconDescription: Int = R.string.device_image,
    @DrawableRes
    icon: Int = R.drawable.ic_menu_devices,
    onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.padding(8.dp).fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = stringResource(iconDescription),
                modifier = Modifier.requiredSize(48.dp)
            )
            Column() {
                Text(
                    name,
                    maxLines = 1,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.subtitle1
                )
            }
        }
    }
}
