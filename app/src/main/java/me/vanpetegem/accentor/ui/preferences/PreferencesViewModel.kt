package me.vanpetegem.accentor.ui.preferences

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.Transformations.switchMap
import me.vanpetegem.accentor.data.AccentorDatabase
import me.vanpetegem.accentor.data.authentication.AuthenticationDataSource
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository
import me.vanpetegem.accentor.data.preferences.PreferencesDataSource
import me.vanpetegem.accentor.data.codecconversions.CodecConversionRepository
import me.vanpetegem.accentor.data.codecconversions.CodecConversion
import me.vanpetegem.accentor.data.users.User
import me.vanpetegem.accentor.data.users.UserRepository

class PreferencesViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AccentorDatabase.getDatabase(application)
    private val authenticationRepository = AuthenticationRepository(AuthenticationDataSource(application))
    private val userRepository = UserRepository(database.userDao(), authenticationRepository)
    private val preferencesDataSource = PreferencesDataSource(application)
    private val codecConversionRepository= CodecConversionRepository(database.codecConversionDao(), authenticationRepository)

    private val conversionId: LiveData<Int> = preferencesDataSource.conversionId

    val currentUser: LiveData<User?> = userRepository.currentUser
    val server: LiveData<String> = authenticationRepository.server
    val imageCacheSize: LiveData<Long> = preferencesDataSource.imageCacheSize
    val musicCacheSize: LiveData<Long> = preferencesDataSource.musicCacheSize
    val conversion: LiveData<CodecConversion> = switchMap(codecConversionRepository.allCodecConversionsById) { ccs ->
        map(conversionId) { it?.let { ccs[it] } } 
    }
    val possibleConversions = codecConversionRepository.allCodecConversions

    fun setMusicCacheSize(newSize: Long) = preferencesDataSource.setMusicCacheSize(newSize)
    fun setImageCacheSize(newSize: Long) = preferencesDataSource.setImageCacheSize(newSize)
    fun setConversionId(newId: Int) = preferencesDataSource.setConversionId(newId)
}
