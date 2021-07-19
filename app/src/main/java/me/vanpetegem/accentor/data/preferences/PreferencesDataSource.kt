package me.vanpetegem.accentor.data.preferences

import android.content.Context
import androidx.lifecycle.LiveData
import me.vanpetegem.accentor.util.longLiveData
import me.vanpetegem.accentor.util.stringLiveData

const val CONVERSION_ID_KEY = "conversion_id"
const val IMAGE_CACHE_SIZE_KEY = "image_cache_size"
const val MUSIC_CACHE_SIZE_KEY = "music_cache_size"

class PreferencesDataSource(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("me.vanpetegem.accentor.preferences", Context.MODE_PRIVATE)

    private val conversionIdData = sharedPreferences.stringLiveData(CONVERSION_ID_KEY)
    private val imageCacheSizeData = sharedPreferences.longLiveData(IMAGE_CACHE_SIZE_KEY, 1024L * 1024L * 1024L)
    private val musicCacheSizeData = sharedPreferences.longLiveData(MUSIC_CACHE_SIZE_KEY, 10L * 1024L * 1024L * 1024L)

    val conversionId: LiveData<String> = conversionIdData
    val imageCacheSize: LiveData<Long> = imageCacheSizeData
    val musicCacheSize: LiveData<Long> = musicCacheSizeData

    fun setConversionId(id: String?) = sharedPreferences.edit().putString(CONVERSION_ID_KEY, id).apply()
    fun setImageCacheSize(size: Long) = sharedPreferences.edit().putLong(IMAGE_CACHE_SIZE_KEY, size).apply()
    fun setMusicCacheSize(size: Long) = sharedPreferences.edit().putLong(MUSIC_CACHE_SIZE_KEY, size).apply()
}
