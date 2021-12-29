package me.vanpetegem.accentor.devices


import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.MutableLiveData
import org.fourthline.cling.android.AndroidUpnpService
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.message.header.ServiceTypeHeader
import org.fourthline.cling.model.meta.RemoteDevice
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.model.types.UDN
import org.fourthline.cling.registry.DefaultRegistryListener
import org.fourthline.cling.registry.Registry
import org.fourthline.cling.support.avtransport.callback.GetDeviceCapabilities
import org.fourthline.cling.support.avtransport.callback.GetMediaInfo
import org.fourthline.cling.support.avtransport.callback.Play
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable
import org.fourthline.cling.support.model.DIDLContent
import org.fourthline.cling.support.model.DIDLObject
import org.fourthline.cling.support.model.DeviceCapabilities
import org.fourthline.cling.support.model.MediaInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceManager @Inject constructor() {

    val playerDevices = MutableLiveData<Map<UDN, Device>>(emptyMap())
    val selectedDevice = MutableLiveData<Device?>(null)

    val connection = DeviceServiceConnection()

    private lateinit var upnp: AndroidUpnpService
    private val isConnected = MutableLiveData(false)
    private val registryListener = DeviceRegistryListener()
    private var discovered: Map<UDN, Device> = emptyMap()

    fun search() {
        upnp.controlPoint.search(ServiceTypeHeader(PLAYER_SERVICE))
    }

    fun select(device: Device) {
        selectedDevice.postValue(device)
        //val url = "http://10.0.0.15:8200/MediaItems/22.mp3"
        val url = "https://rien.maertens.io/noot.mp3"


        val action = SetURI(device, url)
        val future = upnp.controlPoint.execute(action)
    }

    inner class SetURI(val device: Device, uri: String): SetAVTransportURI(device.playerService(), uri) {
        override fun success(invocation: ActionInvocation<out Service<*, *>>?) {
            super.success(invocation)
            Log.e(TAG, "SetURI invocation succeeded: $invocation")
            upnp.controlPoint.execute(Play(device))
        }
        override fun failure(invocation: ActionInvocation<out Service<*, *>>?, operation: UpnpResponse?, defaultMsg: String?) {
            Log.e(TAG, "SetURI invocation failed: $defaultMsg")
        }
    }

    inner class Play(val device: Device): org.fourthline.cling.support.avtransport.callback.Play(device.playerService()) {
        override fun success(invocation: ActionInvocation<out Service<*, *>>?) {
            super.success(invocation)
            Log.e(TAG, "Play invocation succeeded: $invocation")
            upnp.controlPoint.execute(GetInfo(device))
        }
        override fun failure(invocation: ActionInvocation<out Service<*, *>>?, operation: UpnpResponse?, defaultMsg: String?) {
            Log.e(TAG, "Play invocation failed: $defaultMsg")
        }

    }

    inner class GetInfo(val device: Device): GetMediaInfo(device.playerService()) {
        override fun received(invocation: ActionInvocation<out Service<*, *>>?, mediaInfo: MediaInfo?) {
            Log.e(TAG, "GetInfo invocation succeeded: $invocation $mediaInfo")
        }

        override fun failure(invocation: ActionInvocation<out Service<*, *>>?, operation: UpnpResponse?, defaultMsg: String?) {
            Log.e(TAG, "GetInfo invocation failed: $defaultMsg")
        }
    }

    inner class GetCapabilities(val device: Device): GetDeviceCapabilities(device.playerService()) {
        override fun failure(invocation: ActionInvocation<out Service<*, *>>?, operation: UpnpResponse?, defaultMsg: String?) {
            Log.e(TAG, "GetCapabilities invocation failed: $defaultMsg")
        }

        override fun received(actionInvocation: ActionInvocation<out Service<*, *>>?, caps: DeviceCapabilities?) {
            Log.e(TAG, "GetCapabilities invocation succeeded: $actionInvocation $caps")
        }

    }

    inner class DeviceServiceConnection() : ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, binder: IBinder?) {
            upnp = binder!! as AndroidUpnpService
            isConnected.value = true

            // clear devices (if any) and collect the known remote devices into a map
            discovered = upnp.registry.devices
                .filterIsInstance<RemoteDevice>()
                .map { it.identity.udn to Device(it) }
                .toMap()

            playerDevices.postValue(discovered.filter { it.value.isPlayer() })

            upnp.registry.addListener(registryListener)
            search()
        }

        override fun onServiceDisconnected(className: ComponentName?) {
            isConnected.value = false
        }
    }

    private inner class DeviceRegistryListener(): DefaultRegistryListener() {

        override fun remoteDeviceAdded(registry: Registry?, remote: RemoteDevice?) {
            val udn = remote!!.identity.udn
            val dev = Device(remote)
            discovered = discovered + (udn to dev)
            Log.i(TAG, "Device added: $dev")

            if (dev.isPlayer()) {
                playerDevices.postValue(playerDevices.value!! + (udn to dev))
                Log.i(TAG,"Device added to players: $dev")
            }
        }

        override fun remoteDeviceRemoved(registry: Registry?, remote: RemoteDevice?) {
            val udn = remote!!.identity.udn
            Log.i(TAG, "Removing device ${remote.displayString} ($udn)")
            playerDevices.postValue(playerDevices.value!! - udn)
        }
    }
}

const val TAG: String = "DeviceManagexr"
