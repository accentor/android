package me.vanpetegem.accentor.devices

import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.model.meta.RemoteDevice

class Device(
    private val clingDevice: RemoteDevice
) {

    val friendlyName: String = clingDevice.details.friendlyName
    val firstCharacter: String  = String(intArrayOf(friendlyName.codePointAt(0)), 0, 1)

    val type: String = clingDevice.type.displayString

    val imageURL: String? = clingDevice
        .icons
        .maxWithOrNull(compareBy({ it.height * it.width }, { it.mimeType.subtype == "png" }))
        ?.let { clingDevice.normalizeURI(it.uri).toString() }

}
