package me.vanpetegem.accentor

import android.app.Application
import com.github.kittinunf.fuel.core.FuelManager

class Accentor : Application() {
    override fun onCreate() {
        super.onCreate()
        version = applicationContext.packageManager.getPackageInfo(packageName, 0).versionName
        userAgent = "Accentor/$version"
        FuelManager.instance.baseHeaders = mapOf("User-Agent" to userAgent)
    }
}

lateinit var version: String
lateinit var userAgent: String
