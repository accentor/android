package me.vanpetegem.accentor.util

import android.content.SharedPreferences
import androidx.lifecycle.LiveData

abstract class SharedPreferenceLiveData<T>(
    protected val sharedPrefs: SharedPreferences,
    private val key: String
) : LiveData<T>(), SharedPreferences.OnSharedPreferenceChangeListener {

    init {
        value = this.getValueFromPreferences(key)
    }

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

class SharedPreferenceIntLiveData(sharedPrefs: SharedPreferences, key: String) :
    SharedPreferenceLiveData<Int>(sharedPrefs, key) {
    override fun getValueFromPreferences(key: String): Int? =
        if (sharedPrefs.contains(key)) sharedPrefs.getInt(key, 0) else null
}

class SharedPreferenceStringLiveData(sharedPrefs: SharedPreferences, key: String) :
    SharedPreferenceLiveData<String>(sharedPrefs, key) {
    override fun getValueFromPreferences(key: String): String? =
        if (sharedPrefs.contains(key)) sharedPrefs.getString(key, null) else null
}

class SharedPreferenceBooleanLiveData(sharedPrefs: SharedPreferences, key: String) :
    SharedPreferenceLiveData<Boolean>(sharedPrefs, key) {
    override fun getValueFromPreferences(key: String): Boolean? =
        if (sharedPrefs.contains(key)) sharedPrefs.getBoolean(key, false) else null
}

class SharedPreferenceFloatLiveData(sharedPrefs: SharedPreferences, key: String) :
    SharedPreferenceLiveData<Float>(sharedPrefs, key) {
    override fun getValueFromPreferences(key: String): Float? =
        if (sharedPrefs.contains(key)) sharedPrefs.getFloat(key, .0f) else null
}

class SharedPreferenceLongLiveData(sharedPrefs: SharedPreferences, key: String) :
    SharedPreferenceLiveData<Long>(sharedPrefs, key) {
    override fun getValueFromPreferences(key: String): Long? =
        if (sharedPrefs.contains(key)) sharedPrefs.getLong(key, 0) else null
}

fun SharedPreferences.intLiveData(key: String): SharedPreferenceLiveData<Int> {
    return SharedPreferenceIntLiveData(this, key)
}

fun SharedPreferences.stringLiveData(key: String): SharedPreferenceLiveData<String> {
    return SharedPreferenceStringLiveData(this, key)
}

fun SharedPreferences.booleanLiveData(key: String): SharedPreferenceLiveData<Boolean> {
    return SharedPreferenceBooleanLiveData(this, key)
}

fun SharedPreferences.floatLiveData(key: String): SharedPreferenceLiveData<Float> {
    return SharedPreferenceFloatLiveData(this, key)
}

fun SharedPreferences.longLiveData(key: String): SharedPreferenceLiveData<Long> {
    return SharedPreferenceLongLiveData(this, key)
}
