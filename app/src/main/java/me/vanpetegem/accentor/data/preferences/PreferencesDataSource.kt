package me.vanpetegem.accentor.data.preferences

import android.content.Context
import androidx.lifecycle.LiveData
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import me.vanpetegem.accentor.util.intLiveData
import me.vanpetegem.accentor.util.longLiveData

const val CONVERSION_ID_KEY = "conversion_id"
const val IMAGE_CACHE_SIZE_KEY = "image_cache_size"
const val MUSIC_CACHE_SIZE_KEY = "music_cache_size"

class PreferencesDataSource @Inject constructor(@ApplicationContext private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("me.vanpetegem.accentor.preferences", Context.MODE_PRIVATE)

    private val conversionIdData = sharedPreferences.intLiveData(CONVERSION_ID_KEY)
    private val imageCacheSizeData = sharedPreferences.longLiveData(IMAGE_CACHE_SIZE_KEY, 1024L * 1024L * 1024L)
    private val musicCacheSizeData = sharedPreferences.longLiveData(MUSIC_CACHE_SIZE_KEY, 10L * 1024L * 1024L * 1024L)

    val conversionId: LiveData<Int> = conversionIdData
    val imageCacheSize: LiveData<Long> = imageCacheSizeData
    val musicCacheSize: LiveData<Long> = musicCacheSizeData

    fun setConversionId(id: Int) = sharedPreferences.edit().putInt(CONVERSION_ID_KEY, id).apply()
    fun setImageCacheSize(size: Long) = sharedPreferences.edit().putLong(IMAGE_CACHE_SIZE_KEY, size).apply()
    fun setMusicCacheSize(size: Long) = sharedPreferences.edit().putLong(MUSIC_CACHE_SIZE_KEY, size).apply()
}
