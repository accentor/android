package me.vanpetegem.accentor.data.authentication

import android.content.Context

const val ID_KEY = "id"
const val SERVER_KEY = "server"
const val USER_ID_KEY = "user_id"
const val DEVICE_ID_KEY = "device_id"
const val SECRET_KEY = "secret"

class AuthenticationDataSource(context: Context) {
    private val sharedPreferences =
        context.getSharedPreferences("me.vanpetegem.accentor.authenticationData", Context.MODE_PRIVATE)

    var authData: AuthenticationData?
        get() {
            if (sharedPreferences.contains(ID_KEY) &&
                sharedPreferences.contains(USER_ID_KEY) &&
                sharedPreferences.contains(DEVICE_ID_KEY) &&
                sharedPreferences.contains(SECRET_KEY)
            ) {
                val deviceId = sharedPreferences.getString(DEVICE_ID_KEY, "") ?: return null
                val secret = sharedPreferences.getString(SECRET_KEY, "") ?: return null
                return AuthenticationData(
                    sharedPreferences.getInt(ID_KEY, 0),
                    sharedPreferences.getInt(USER_ID_KEY, 0),
                    deviceId,
                    secret
                )
            }
            return null
        }
        set(value) {
            if (value == null) {
                sharedPreferences.edit()
                    .remove(ID_KEY)
                    .remove(USER_ID_KEY)
                    .remove(DEVICE_ID_KEY)
                    .remove(SECRET_KEY)
                    .apply()
            } else {
                sharedPreferences.edit()
                    .putInt(ID_KEY, value.id)
                    .putInt(USER_ID_KEY, value.userId)
                    .putString(DEVICE_ID_KEY, value.deviceId)
                    .putString(SECRET_KEY, value.secret)
                    .apply()
            }
        }

    var server: String?
        get() {
            return sharedPreferences.getString(SERVER_KEY, null)
        }
        set(value) {
            sharedPreferences.edit()
                .putString(SERVER_KEY, value)
                .apply()
        }

}