package me.vanpetegem.accentor.ui.devices

import android.app.Application
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import me.vanpetegem.accentor.devices.Device
import me.vanpetegem.accentor.devices.DeviceRegistryListener
import org.fourthline.cling.android.AndroidUpnpService

class DevicesViewModel(application: Application) : AndroidViewModel(application) {}
