package me.vanpetegem.accentor.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.time.LocalDate
import me.vanpetegem.accentor.data.AccentorDatabase
import me.vanpetegem.accentor.data.albums.AlbumRepository
import me.vanpetegem.accentor.data.authentication.AuthenticationDataSource
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val authenticationRepository = AuthenticationRepository(AuthenticationDataSource(application))
    private val database = AccentorDatabase.getDatabase(application)
    private val albumRepository = AlbumRepository(database.albumDao(), authenticationRepository)

    private val _currentDay = MutableLiveData<LocalDate>(LocalDate.now())

    val recentlyReleasedAlbums = albumRepository.albumsByReleased
    val recentlyAddedAlbums = albumRepository.albumsByAdded
    val randomAlbums = albumRepository.randomAlbums
    val currentDay: LiveData<LocalDate> = _currentDay

    fun albumsForDay(day: LocalDate) = albumRepository.findByDay(day)
    fun updateCurrentDay() = _currentDay.postValue(LocalDate.now())
}
