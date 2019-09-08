package me.vanpetegem.accentor

import android.app.Application
import com.github.kittinunf.fuel.core.FuelManager
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import java.io.File

class Accentor : Application() {
    override fun onCreate() {
        super.onCreate()
        userAgent = "Accentor/${applicationContext.packageManager.getPackageInfo(packageName, 0).versionName}"
        FuelManager.instance.baseHeaders = mapOf("User-Agent" to userAgent)

        audioDatabaseProvider = ExoDatabaseProvider(this)
        audioCache = SimpleCache(
            File(cacheDir, "audio"),
            LeastRecentlyUsedCacheEvictor(10L * 1024L * 1024L * 1024L),
            audioDatabaseProvider
        )

    }
}

lateinit var userAgent: String
lateinit var audioCache: SimpleCache
lateinit var audioDatabaseProvider: ExoDatabaseProvider