package me.vanpetegem.accentor.ui.devices

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import dagger.hilt.android.lifecycle.HiltViewModel
import me.vanpetegem.accentor.devices.Device
import me.vanpetegem.accentor.devices.DeviceManager
import javax.inject.Inject

@HiltViewModel
class DevicesViewModel @Inject constructor(
    application: Application,
    private val deviceManager: DeviceManager,
) : AndroidViewModel(application) {

    fun devices(): LiveData<List<Device>> = map(deviceManager.playerDevices) { devices ->
        devices.values.sortedWith(compareBy { it.friendlyName })
    }

    fun selectDevice(device: Device) {
        deviceManager.select(device = device)
    }
}
