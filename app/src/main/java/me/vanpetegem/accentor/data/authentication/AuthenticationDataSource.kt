package me.vanpetegem.accentor.data.authentication

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import dagger.hilt.android.qualifiers.ApplicationContext
import me.vanpetegem.accentor.util.intLiveData
import me.vanpetegem.accentor.util.stringLiveData
import javax.inject.Inject

const val ID_KEY = "id"
const val SERVER_KEY = "server"
const val USER_ID_KEY = "user_id"
const val TOKEN_KEY = "token"

class AuthenticationDataSource
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) {
        private val sharedPreferences =
            context.getSharedPreferences("me.vanpetegem.accentor.authenticationData", Context.MODE_PRIVATE)

        private val idData = sharedPreferences.intLiveData(ID_KEY)
        private val userIdData = sharedPreferences.intLiveData(USER_ID_KEY)
        private val tokenData = sharedPreferences.stringLiveData(TOKEN_KEY)

        private val serverData = sharedPreferences.stringLiveData(SERVER_KEY)

        val authData: LiveData<AuthenticationData?>
        val server: LiveData<String?> = serverData

        init {
            authData =
                MediatorLiveData<AuthenticationData?>().apply {
                    val observer: Observer<Any?> =
                        Observer {
                            val id: Int =
                                idData.value.let {
                                    if (it != null) {
                                        it
                                    } else {
                                        value = null
                                        return@Observer
                                    }
                                }
                            val userId: Int =
                                userIdData.value.let {
                                    if (it != null) {
                                        it
                                    } else {
                                        value = null
                                        return@Observer
                                    }
                                }
                            val token: String =
                                tokenData.value.let {
                                    if (it != null) {
                                        it
                                    } else {
                                        value = null
                                        return@Observer
                                    }
                                }
                            val newVal = AuthenticationData(id, userId, token)
                            if (newVal != this.value) this.value = newVal
                        }

                    addSource(idData, observer)
                    addSource(userIdData, observer)
                    addSource(tokenData, observer)
                    // If we don't do this, the value will start out as null even if we have data in the prefs.
                    observer.onChanged(null)
                }
        }

        fun setAuthData(authData: AuthenticationData?) {
            if (authData == null) {
                sharedPreferences.edit {
                    remove(ID_KEY)
                    remove(USER_ID_KEY)
                    remove(TOKEN_KEY)
                }
            } else {
                sharedPreferences.edit {
                    putInt(ID_KEY, authData.id)
                    putInt(USER_ID_KEY, authData.userId)
                    putString(TOKEN_KEY, authData.token)
                }
            }
        }

        fun setServer(server: String?) = sharedPreferences.edit { putString(SERVER_KEY, server) }

        fun getServer(): String? = sharedPreferences.getString(SERVER_KEY, null)

        fun getToken(): String? = sharedPreferences.getString(TOKEN_KEY, null)
    }
