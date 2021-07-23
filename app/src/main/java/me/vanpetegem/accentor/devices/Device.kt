package me.vanpetegem.accentor.devices

import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.model.meta.Service

class Device(
    private val device: Device<*, *, *>
) {

    fun firstCharacter() = String(intArrayOf(displayString().codePointAt(0)), 0, 1)

    fun displayString(): String {
        return device.details.friendlyName
    }

    fun type(): String {
        return device.type.displayString
    }
}
