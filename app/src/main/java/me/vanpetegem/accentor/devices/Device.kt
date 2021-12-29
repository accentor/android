package me.vanpetegem.accentor.devices

import androidx.compose.foundation.lazy.rememberLazyListState
import org.fourthline.cling.model.meta.RemoteDevice
import org.fourthline.cling.model.meta.RemoteService
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.model.types.ServiceType
import org.fourthline.cling.model.types.UDN
import java.lang.Exception

val PLAYER_SERVICE = ServiceType("schemas-upnp-org", "AVTransport", 1);

class Device(
    protected val clingDevice: RemoteDevice
) {

    val friendlyName: String = clingDevice.details.friendlyName
    val displayString: String = clingDevice.displayString
    val type: String = clingDevice.type.displayString
    val udn: UDN = clingDevice.identity.udn

    val imageURL: String? = clingDevice
        .icons
        .maxWithOrNull(compareBy({ it.height * it.width }, { it.mimeType.subtype == "png" }))
        ?.let { clingDevice.normalizeURI(it.uri).toString() }

    fun isPlayer(): Boolean {
        return clingDevice.findServiceTypes().contains(PLAYER_SERVICE)
    }

    fun isHydrated(): Boolean {
        return playerService()?.hasActions() == true
    }

    fun playerService(): RemoteService? {
        return clingDevice.findService(PLAYER_SERVICE)
    }

    override fun toString(): String {
        return "Device($friendlyName, ${clingDevice.findServiceTypes().map { it.type }})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Device

        if (udn != other.udn) return false

        return true
    }

    override fun hashCode(): Int {
        return udn.hashCode()
    }


}




