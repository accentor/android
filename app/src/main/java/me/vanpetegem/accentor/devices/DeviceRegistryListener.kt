package me.vanpetegem.accentor.devices


import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.LiveData
import org.fourthline.cling.model.meta.LocalDevice
import org.fourthline.cling.model.meta.RemoteDevice
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.registry.DefaultRegistryListener
import org.fourthline.cling.registry.Registry
import java.lang.Exception
import org.fourthline.cling.model.meta.Device as ClingDevice

class DeviceRegistryListener: DefaultRegistryListener() {

    val devices: SnapshotStateList<Device> = mutableStateListOf()

    override fun remoteDeviceDiscoveryStarted(registry: Registry?, device: RemoteDevice?) {
        addDevice(device)
    }

    override fun remoteDeviceDiscoveryFailed(registry: Registry?, device: RemoteDevice?, ex: Exception?) {
        removeDevice(device)
    }

    override fun remoteDeviceAdded(registry: Registry?, device: RemoteDevice?) {
        addDevice(device)
    }

    override fun localDeviceAdded(registry: Registry?, device: LocalDevice?) {
        addDevice(device)
    }

    override fun localDeviceRemoved(registry: Registry?, device: LocalDevice?) {
        removeDevice(device)
    }

    fun addDevice(
        device: ClingDevice<*, out ClingDevice<*, *, *>, out Service<*, *>>?
    ) {
        val d = Device(device!!)
        // update?
        devices.add(d)
    }

    fun removeDevice(
        device: org.fourthline.cling.model.meta.Device<*, out org.fourthline.cling.model.meta.Device<*, *, *>, out Service<*, *>>?
    ) {
        val d = Device(device!!)
        devices.remove(d)
    }


}
