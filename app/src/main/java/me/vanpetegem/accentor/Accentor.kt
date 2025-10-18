package me.vanpetegem.accentor

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.disk.DiskCache
import com.github.kittinunf.fuel.core.FuelManager
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.HiltAndroidApp
import me.vanpetegem.accentor.data.preferences.PreferencesDataSource
import org.acra.config.dialog
import org.acra.config.mailSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import java.io.File
import javax.inject.Inject

@HiltAndroidApp
class Accentor :
    Application(),
    ImageLoaderFactory {
    @Inject lateinit var preferences: PreferencesDataSource

    override fun onCreate() {
        super.onCreate()
        version =
            if (Build.VERSION.SDK_INT >= 33) {
                applicationContext.packageManager
                    .getPackageInfo(
                        packageName,
                        PackageManager.PackageInfoFlags.of(0),
                    ).versionName!!
            } else {
                applicationContext.packageManager.getPackageInfo(packageName, 0).versionName!!
            }
        userAgent = "Accentor/$version"
        FuelManager.instance.baseHeaders = mapOf("User-Agent" to userAgent)
        DynamicColors.applyToActivitiesIfAvailable(this)
    }

    override fun newImageLoader(): ImageLoader =
        ImageLoader
            .Builder(applicationContext)
            .diskCache {
                DiskCache
                    .Builder()
                    .directory(File(dataDir, "coil_image_cache"))
                    .maxSizeBytes(preferences.imageCacheSize.value!!)
                    .build()
            }.components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }.build()

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)

        initAcra {
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.JSON
            dialog {
                title = getString(R.string.error_report_dialog_title)
                text = getString(R.string.error_report_dialog_text)
            }
            mailSender {
                mailTo = "android@accentor.tech"
                subject = "[Accentor Android] Crash report"
            }
        }
    }
}

lateinit var version: String
lateinit var userAgent: String
