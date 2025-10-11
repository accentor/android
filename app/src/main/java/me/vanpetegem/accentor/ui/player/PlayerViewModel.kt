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
        private val _showQueue = MutableLiveData<Boolean>(false)
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

        suspend fun stop() = mediaSessionConnection.stop()

        suspend fun play(album: Album) = mediaSessionConnection.play(album)

        suspend fun play(track: Track) = mediaSessionConnection.play(track)

        suspend fun play(playlist: Playlist) = mediaSessionConnection.play(playlist)

        suspend fun addTrackToQueue(track: Track) = mediaSessionConnection.addTrackToQueue(track)

        suspend fun addTracksToQueue(album: Album) = mediaSessionConnection.addTracksToQueue(album)

        suspend fun addTracksToQueue(playlist: Playlist) = mediaSessionConnection.addTracksToQueue(playlist)

        suspend fun addTrackToQueue(
            track: Track,
            index: Int,
        ) = mediaSessionConnection.addTrackToQueue(track, index)

        suspend fun addTracksToQueue(
            album: Album,
            index: Int,
        ) = mediaSessionConnection.addTracksToQueue(album, index)

        suspend fun clearQueue() = mediaSessionConnection.clearQueue()

        suspend fun previous() = mediaSessionConnection.previous()

        suspend fun pause() = mediaSessionConnection.pause()

        suspend fun play() = mediaSessionConnection.play()

        suspend fun next() = mediaSessionConnection.next()

        suspend fun seekTo(time: Int) = mediaSessionConnection.seekTo(time)

        suspend fun skipTo(position: Int) = mediaSessionConnection.skipTo(position)

        suspend fun removeFromQueue(position: Int) = mediaSessionConnection.removeFromQueue(position)

        suspend fun setRepeatMode(repeatMode: Int) = mediaSessionConnection.setRepeatMode(repeatMode)

        suspend fun setShuffleMode(shuffleMode: Boolean) = mediaSessionConnection.setShuffleMode(shuffleMode)

        suspend fun updateCurrentPosition() = mediaSessionConnection.updateCurrentPosition()
    }
