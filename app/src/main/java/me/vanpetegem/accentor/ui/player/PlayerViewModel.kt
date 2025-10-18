package me.vanpetegem.accentor.ui.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import me.vanpetegem.accentor.data.albums.Album
import me.vanpetegem.accentor.data.playlists.Playlist
import me.vanpetegem.accentor.data.tracks.Track
import me.vanpetegem.accentor.media.MediaSessionConnection
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel
    @Inject
    constructor(
        application: Application,
        private val mediaSessionConnection: MediaSessionConnection,
    ) : AndroidViewModel(application) {
        private val _showQueue = MutableLiveData(false)
        val showQueue: LiveData<Boolean> = _showQueue

        val currentTrack = mediaSessionConnection.currentTrack
        val currentAlbum = mediaSessionConnection.currentAlbum
        val currentPosition = mediaSessionConnection.currentPosition
        val playing = mediaSessionConnection.playing
        val buffering = mediaSessionConnection.buffering
        val repeatMode = mediaSessionConnection.repeatMode
        val shuffleMode = mediaSessionConnection.shuffleMode
        val queue = mediaSessionConnection.queue
        val queueLength = mediaSessionConnection.queueLength
        val queuePosition = mediaSessionConnection.queuePosition
        val queuePosStr = mediaSessionConnection.queuePosStr

        fun toggleQueue() {
            _showQueue.value = !(_showQueue.value ?: false)
        }

        fun play(album: Album) = mediaSessionConnection.play(album)

        fun play(track: Track) = mediaSessionConnection.play(track)

        fun play(playlist: Playlist) = mediaSessionConnection.play(playlist)

        fun addTrackToQueue(track: Track) = mediaSessionConnection.addTrackToQueue(track)

        fun addTracksToQueue(album: Album) = mediaSessionConnection.addTracksToQueue(album)

        fun addTracksToQueue(playlist: Playlist) = mediaSessionConnection.addTracksToQueue(playlist)

        fun addTrackToQueue(
            track: Track,
            index: Int,
        ) = mediaSessionConnection.addTrackToQueue(track, index)

        fun addTracksToQueue(
            album: Album,
            index: Int,
        ) = mediaSessionConnection.addTracksToQueue(album, index)

        fun clearQueue() = mediaSessionConnection.clearQueue()

        fun previous() = mediaSessionConnection.previous()

        fun pause() = mediaSessionConnection.pause()

        fun play() = mediaSessionConnection.play()

        fun next() = mediaSessionConnection.next()

        fun seekTo(time: Int) = mediaSessionConnection.seekTo(time)

        fun skipTo(position: Int) = mediaSessionConnection.skipTo(position)

        fun removeFromQueue(position: Int) = mediaSessionConnection.removeFromQueue(position)

        fun setRepeatMode(repeatMode: Int) = mediaSessionConnection.setRepeatMode(repeatMode)

        fun setShuffleMode(shuffleMode: Boolean) = mediaSessionConnection.setShuffleMode(shuffleMode)

        fun updateCurrentPosition() = mediaSessionConnection.updateCurrentPosition()
    }
