package me.vanpetegem.accentor.ui.devices

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class DevicesViewModel(application: Application) : AndroidViewModel(application) {
    val allDevices = listOf<String>("Frigo", "Koelkast", "Ijstkast");
}
