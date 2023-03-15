package me.vanpetegem.accentor.util

import android.content.SharedPreferences
import androidx.lifecycle.LiveData

abstract class SharedPreferenceLiveData<T>(
    protected val sharedPrefs: SharedPreferences,
    private val key: String
) : LiveData<T>(), SharedPreferences.OnSharedPreferenceChangeListener {
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

    override fun onSharedPreferenceChanged(_sp: SharedPreferences?, key: String?) {
        if (key == this.key) {
            value = getValueFromPreferences(this.key)
        }
    }
}

class SharedPreferenceIntLiveData(sharedPrefs: SharedPreferences, key: String, private val default: Int?) :
    SharedPreferenceLiveData<Int?>(sharedPrefs, key) {
    init {
        value = this.getValueFromPreferences(key)
    }

    override fun getValueFromPreferences(key: String): Int? =
        if (sharedPrefs.contains(key)) sharedPrefs.getInt(key, 0) else default
}

class SharedPreferenceStringLiveData(sharedPrefs: SharedPreferences, key: String, private val default: String?) :
    SharedPreferenceLiveData<String?>(sharedPrefs, key) {
    init {
        value = this.getValueFromPreferences(key)
    }

    override fun getValueFromPreferences(key: String): String? =
        if (sharedPrefs.contains(key)) sharedPrefs.getString(key, default) else default
}

class SharedPreferenceBooleanLiveData(sharedPrefs: SharedPreferences, key: String, private val default: Boolean?) :
    SharedPreferenceLiveData<Boolean?>(sharedPrefs, key) {
    init {
        value = this.getValueFromPreferences(key)
    }

    override fun getValueFromPreferences(key: String): Boolean? =
        if (sharedPrefs.contains(key)) sharedPrefs.getBoolean(key, false) else default
}

class SharedPreferenceFloatLiveData(sharedPrefs: SharedPreferences, key: String, private val default: Float?) :
    SharedPreferenceLiveData<Float?>(sharedPrefs, key) {
    init {
        value = this.getValueFromPreferences(key)
    }

    override fun getValueFromPreferences(key: String): Float? =
        if (sharedPrefs.contains(key)) sharedPrefs.getFloat(key, .0f) else default
}

class SharedPreferenceLongLiveData(sharedPrefs: SharedPreferences, key: String, private val default: Long?) :
    SharedPreferenceLiveData<Long?>(sharedPrefs, key) {
    init {
        value = this.getValueFromPreferences(key)
    }

    override fun getValueFromPreferences(key: String): Long? {
        return if (sharedPrefs.contains(key)) sharedPrefs.getLong(key, 0L) else default
    }
}

fun SharedPreferences.intLiveData(key: String, default: Int? = null): SharedPreferenceLiveData<Int?> {
    return SharedPreferenceIntLiveData(this, key, default)
}

fun SharedPreferences.stringLiveData(key: String, default: String? = null): SharedPreferenceLiveData<String?> {
    return SharedPreferenceStringLiveData(this, key, default)
}

fun SharedPreferences.booleanLiveData(key: String, default: Boolean? = null): SharedPreferenceLiveData<Boolean?> {
    return SharedPreferenceBooleanLiveData(this, key, default)
}

fun SharedPreferences.floatLiveData(key: String, default: Float? = null): SharedPreferenceLiveData<Float?> {
    return SharedPreferenceFloatLiveData(this, key, default)
}

fun SharedPreferences.longLiveData(key: String, default: Long? = null): SharedPreferenceLiveData<Long?> {
    return SharedPreferenceLongLiveData(this, key, default)
}
