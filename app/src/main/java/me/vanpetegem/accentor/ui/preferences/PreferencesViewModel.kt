package me.vanpetegem.accentor.ui.preferences

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.Transformations.switchMap
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository
import me.vanpetegem.accentor.data.codecconversions.CodecConversion
import me.vanpetegem.accentor.data.codecconversions.CodecConversionRepository
import me.vanpetegem.accentor.data.preferences.PreferencesDataSource
import me.vanpetegem.accentor.data.users.User
import me.vanpetegem.accentor.data.users.UserRepository

@HiltViewModel
class PreferencesViewModel @Inject constructor(
    application: Application,
    private val preferencesDataSource: PreferencesDataSource,
    private val userRepository: UserRepository,
    private val authenticationRepository: AuthenticationRepository,
    private val codecConversionRepository: CodecConversionRepository,
) : AndroidViewModel(application) {
    private val conversionId: LiveData<Int> = preferencesDataSource.conversionId

    val currentUser: LiveData<User?> = userRepository.currentUser
    val server: LiveData<String> = authenticationRepository.server
    val imageCacheSize: LiveData<Long> = preferencesDataSource.imageCacheSize
    val musicCacheSize: LiveData<Long> = preferencesDataSource.musicCacheSize
    val lastSyncFinished: LiveData<Instant> = preferencesDataSource.lastSyncFinished
    val conversion: LiveData<CodecConversion> = switchMap(codecConversionRepository.allCodecConversionsById) { ccsMap ->
        switchMap(codecConversionRepository.allCodecConversions) { ccs ->
            map(conversionId) { it?.let { ccsMap[it] } ?: ccs.firstOrNull() }
        }
    }
    val possibleConversions = codecConversionRepository.allCodecConversions

    fun setMusicCacheSize(newSize: Long) = preferencesDataSource.setMusicCacheSize(newSize)
    fun setImageCacheSize(newSize: Long) = preferencesDataSource.setImageCacheSize(newSize)
    fun setConversionId(newId: Int) = preferencesDataSource.setConversionId(newId)
}
