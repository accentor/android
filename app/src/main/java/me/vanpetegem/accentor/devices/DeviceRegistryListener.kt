package me.vanpetegem.accentor.devices


import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.LiveData
import org.fourthline.cling.model.meta.LocalDevice
import org.fourthline.cling.model.meta.RemoteDevice
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.model.types.UDN
import org.fourthline.cling.registry.DefaultRegistryListener
import org.fourthline.cling.registry.Registry
import java.lang.Exception
import org.fourthline.cling.model.meta.Device as ClingDevice

class DeviceRegistryListener: DefaultRegistryListener() {

    val devices: SnapshotStateMap<UDN, Device> = mutableStateMapOf()

    override fun remoteDeviceDiscoveryStarted(registry: Registry?, remote: RemoteDevice?) {
        // TODO
    }

    override fun remoteDeviceDiscoveryFailed(registry: Registry?, remote: RemoteDevice?, ex: Exception?) {
        // TODO
    }

    override fun remoteDeviceUpdated(registry: Registry?, device: RemoteDevice?) {
        // TODO
    }

    override fun remoteDeviceAdded(registry: Registry?, remote: RemoteDevice?) {
        addDevice(remote)
    }

    override fun remoteDeviceRemoved(registry: Registry?, remote: RemoteDevice?) {
        removeDevice(remote)
    }

    fun addDevice(remote: RemoteDevice?) {
        val device = Device(remote!!)
        devices[remote.identity.udn] = device
    }

    fun removeDevice(remote: RemoteDevice?) {
        devices.remove(remote!!.identity.udn)
    }


}
