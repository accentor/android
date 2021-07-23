package me.vanpetegem.accentor.devices

import org.fourthline.cling.UpnpServiceConfiguration
import org.fourthline.cling.android.AndroidUpnpServiceConfiguration
import org.fourthline.cling.android.AndroidUpnpServiceImpl
import org.fourthline.cling.binding.xml.ServiceDescriptorBinder
import org.fourthline.cling.binding.xml.UDA10DeviceDescriptorBinderImpl
import org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderImpl

class DeviceService: AndroidUpnpServiceImpl() {

    override fun createConfiguration(): UpnpServiceConfiguration {
        return object: AndroidUpnpServiceConfiguration() {
            // This override fixes the XML parser
            // See https://github.com/4thline/cling/issues/247
            override fun getServiceDescriptorBinderUDA10(): ServiceDescriptorBinder {
                return UDA10ServiceDescriptorBinderImpl()
            }
        }
    }

}
