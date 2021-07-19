package me.vanpetegem.accentor.ui.preferences

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import me.vanpetegem.accentor.data.AccentorDatabase
import me.vanpetegem.accentor.data.authentication.AuthenticationDataSource
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository
import me.vanpetegem.accentor.data.preferences.PreferencesDataSource
import me.vanpetegem.accentor.data.users.User
import me.vanpetegem.accentor.data.users.UserRepository

class PreferencesViewModel(application: Application) : AndroidViewModel(application) {
    private val authenticationRepository = AuthenticationRepository(AuthenticationDataSource(application))
    private val preferencesDataSource = PreferencesDataSource(application)

    val currentUser: LiveData<User?>
    val server: LiveData<String> = authenticationRepository.server
    val imageCacheSize: LiveData<Long> = preferencesDataSource.imageCacheSize
    val musicCacheSize: LiveData<Long> = preferencesDataSource.musicCacheSize
    val conversionId: LiveData<String> = preferencesDataSource.conversionId

    init {
        val database = AccentorDatabase.getDatabase(application)
        val userRepository = UserRepository(database.userDao(), authenticationRepository)
        currentUser = userRepository.currentUser
    }

    fun setMusicCacheSize(newSize: Long) = preferencesDataSource.setMusicCacheSize(newSize)
    fun setImageCacheSize(newSize: Long) = preferencesDataSource.setImageCacheSize(newSize)
}
