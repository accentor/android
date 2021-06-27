package me.vanpetegem.accentor

import android.app.Application
import com.github.kittinunf.fuel.core.FuelManager

class Accentor : Application() {
    override fun onCreate() {
        super.onCreate()
        userAgent = "Accentor/${applicationContext.packageManager.getPackageInfo(packageName, 0).versionName}"
        FuelManager.instance.baseHeaders = mapOf("User-Agent" to userAgent)
    }
}

lateinit var userAgent: String
