package me.vanpetegem.accentor.util

import android.content.SharedPreferences
import androidx.lifecycle.LiveData

abstract class SharedPreferenceLiveData<T>(
    protected val sharedPrefs: SharedPreferences,
    private val key: String,
) : LiveData<T>(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    abstract fun getValueFromPreferences(key: String): T?

    override fun onActive() {
        super.onActive()
        value = getValueFromPreferences(key)
        sharedPrefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onInactive() {
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(this)
        super.onInactive()
    }

    override fun onSharedPreferenceChanged(
        _sp: SharedPreferences?,
        key: String?,
    ) {
        if (key == this.key) {
            value = getValueFromPreferences(this.key)
        }
    }
}

class SharedPreferenceIntLiveData(
    sharedPrefs: SharedPreferences,
    key: String,
    private val default: Int?,
) : SharedPreferenceLiveData<Int?>(sharedPrefs, key) {
    init {
        value = this.getValueFromPreferences(key)
    }

    override fun getValueFromPreferences(key: String): Int? = if (sharedPrefs.contains(key)) sharedPrefs.getInt(key, 0) else default
}

class SharedPreferenceStringLiveData(
    sharedPrefs: SharedPreferences,
    key: String,
    private val default: String?,
) : SharedPreferenceLiveData<String?>(sharedPrefs, key) {
    init {
        value = this.getValueFromPreferences(key)
    }

    override fun getValueFromPreferences(key: String): String? = if (sharedPrefs.contains(key)) sharedPrefs.getString(key, default) else default
}

class SharedPreferenceLongLiveData(
    sharedPrefs: SharedPreferences,
    key: String,
    private val default: Long?,
) : SharedPreferenceLiveData<Long?>(sharedPrefs, key) {
    init {
        value = this.getValueFromPreferences(key)
    }

    override fun getValueFromPreferences(key: String): Long? = if (sharedPrefs.contains(key)) sharedPrefs.getLong(key, 0L) else default
}

fun SharedPreferences.intLiveData(
    key: String,
    default: Int? = null,
): SharedPreferenceLiveData<Int?> = SharedPreferenceIntLiveData(this, key, default)

fun SharedPreferences.stringLiveData(
    key: String,
    default: String? = null,
): SharedPreferenceLiveData<String?> = SharedPreferenceStringLiveData(this, key, default)

fun SharedPreferences.longLiveData(
    key: String,
    default: Long? = null,
): SharedPreferenceLiveData<Long?> = SharedPreferenceLongLiveData(this, key, default)
