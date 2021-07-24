package me.vanpetegem.accentor.devices

import org.fourthline.cling.model.meta.RemoteDevice
import java.lang.Exception

sealed class Device(
    protected val clingDevice: RemoteDevice
) {

    val friendlyName: String = clingDevice.details.friendlyName
    val displayString: String = clingDevice.displayString
    val type: String = clingDevice.type.displayString

    val imageURL: String? = clingDevice
        .icons
        .maxWithOrNull(compareBy({ it.height * it.width }, { it.mimeType.subtype == "png" }))
        ?.let { clingDevice.normalizeURI(it.uri).toString() }


    class Ready(clingDevice: RemoteDevice): Device(clingDevice) {}

    class Failed(clingDevice: RemoteDevice, val exception: Exception?): Device(clingDevice) {}

    class Discovered(clingDevice: RemoteDevice): Device(clingDevice) {
        fun failed(exception: Exception?): Failed {
            return Failed(clingDevice, exception)
        }

        fun ready(): Ready {
            return Ready(clingDevice)
        }
    }
}


