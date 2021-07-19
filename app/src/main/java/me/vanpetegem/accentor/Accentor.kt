package me.vanpetegem.accentor

import android.app.Application
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.github.kittinunf.fuel.core.FuelManager
import java.io.File
import me.vanpetegem.accentor.data.preferences.PreferencesDataSource
import okhttp3.Cache
import okhttp3.OkHttpClient

class Accentor : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        version = applicationContext.packageManager.getPackageInfo(packageName, 0).versionName
        userAgent = "Accentor/$version"
        FuelManager.instance.baseHeaders = mapOf("User-Agent" to userAgent)
    }

    override fun newImageLoader(): ImageLoader {
        val preferences = PreferencesDataSource(applicationContext)
        return ImageLoader.Builder(applicationContext)
            .okHttpClient {
                OkHttpClient.Builder()
                    .cache(
                        Cache(
                            directory = File(cacheDir, "okhttp_image_cache"),
                            maxSize = preferences.imageCacheSize.value!!
                        )
                    )
                    .build()
            }
            .componentRegistry {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder(applicationContext))
                } else {
                    add(GifDecoder())
                }
            }
            .build()
    }
}

lateinit var version: String
lateinit var userAgent: String
