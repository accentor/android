package me.vanpetegem.accentor.ui.devices

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun Devices(devicesViewModel: DevicesViewModel = viewModel()) {
   Text(devicesViewModel.allDevices.joinToString(separator = ", "))
}
