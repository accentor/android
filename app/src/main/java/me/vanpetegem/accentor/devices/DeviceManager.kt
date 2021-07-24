package me.vanpetegem.accentor.devices


import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.fourthline.cling.android.AndroidUpnpService
import org.fourthline.cling.model.message.header.ServiceTypeHeader
import org.fourthline.cling.model.meta.RemoteDevice
import org.fourthline.cling.model.types.ServiceType
import org.fourthline.cling.model.types.UDN
import org.fourthline.cling.registry.DefaultRegistryListener
import org.fourthline.cling.registry.Registry
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceManager @Inject constructor() {

    val devices = MutableLiveData<Map<UDN, Device>>(emptyMap())
    val selectedDevice = MutableLiveData<Device?>(null)

    val connection = DeviceServiceConnection()

    private lateinit var upnp: AndroidUpnpService
    private val isConnected = MutableLiveData(false)
    private val registryListener = DeviceRegistryListener()

    fun search() {
        val playerService = ServiceTypeHeader(ServiceType("schemas-upnp-org", "AVTransport", 1))
        upnp.controlPoint.search(playerService)
    }

    inner class DeviceServiceConnection() : ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, binder: IBinder?) {
            upnp = binder!! as AndroidUpnpService
            isConnected.value = true

            // clear devices (if any) and collect the known remote devices into a map
            devices.value = upnp.registry.devices
                .filterIsInstance<RemoteDevice>()
                .map { it.identity.udn to Device.Ready(it) }
                .toMap()

            upnp.registry.addListener(registryListener)
            search()
        }

        override fun onServiceDisconnected(className: ComponentName?) {
            isConnected.value = false
        }
    }

    private inner class DeviceRegistryListener(): DefaultRegistryListener() {

        override fun remoteDeviceDiscoveryStarted(registry: Registry?, remote: RemoteDevice?) {
            val udn = remote!!.identity.udn
            // this will only add a new device if not yet present in the map
            devices.postValue(mapOf(udn to Device.Discovered(remote)) + devices.value!!)
        }

        override fun remoteDeviceDiscoveryFailed(registry: Registry?, remote: RemoteDevice?, ex: Exception?) {
            val udn = remote!!.identity.udn
            val known = devices.value!!
            when(val dev = known[udn]) {
                is Device.Discovered -> devices.postValue(known + (udn to dev.failed(ex)))
                else -> Log.e(TAG, "Discovery failed of existing device", ex)
            }
        }

        override fun remoteDeviceUpdated(registry: Registry?, remote: RemoteDevice?) {
            if (devices.value!!.contains(remote!!.identity.udn)) {
                // trigger an update
                devices.postValue(devices.value)
            } else {
                Log.e(TAG, "Non-existing device updated")
            }
        }

        override fun remoteDeviceAdded(registry: Registry?, remote: RemoteDevice?) {
            addDevice(remote!!)
        }

        override fun remoteDeviceRemoved(registry: Registry?, remote: RemoteDevice?) {
            val withRemoved = devices.value!!.minus(remote!!.identity.udn)
            devices.postValue(withRemoved)
        }

        fun addDevice(remote: RemoteDevice) {
            val udn = remote.identity.udn
            val known = devices.value!!
            if (udn in known) {
                when (val dev = known[udn]) {
                    is Device.Discovered -> devices.postValue(known + (udn to dev.ready()))
                    else -> Log.e(TAG, "Device added twice, ignoring... ${remote.displayString} ($udn)")
                }
            } else {
                devices.postValue(known + (udn to Device.Ready(remote)))
            }
        }
    }
}

const val TAG: String = "DeviceManager"
