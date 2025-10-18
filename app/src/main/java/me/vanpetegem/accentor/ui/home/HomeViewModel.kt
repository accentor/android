package me.vanpetegem.accentor.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import me.vanpetegem.accentor.data.albums.AlbumRepository
import me.vanpetegem.accentor.data.artists.ArtistRepository
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        application: Application,
        private val albumRepository: AlbumRepository,
        artistRepository: ArtistRepository,
    ) : AndroidViewModel(application) {
        private val _currentDay = MutableLiveData(LocalDate.now())

        val recentlyReleasedAlbums = albumRepository.albumsByReleased
        val recentlyAddedAlbums = albumRepository.albumsByAdded
        val recentlyPlayedAlbums = albumRepository.albumsByPlayed
        val randomAlbums = albumRepository.randomAlbums
        val recentlyAddedArtists = artistRepository.artistsByAdded
        val recentlyPlayedArtists = artistRepository.artistsByPlayed
        val randomArtists = artistRepository.randomArtists
        val currentDay: LiveData<LocalDate> = _currentDay

        fun albumsForDay(day: LocalDate) = albumRepository.findByDay(day)

        fun updateCurrentDay() = _currentDay.postValue(LocalDate.now())
    }
