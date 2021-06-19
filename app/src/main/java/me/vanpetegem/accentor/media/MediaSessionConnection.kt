package me.vanpetegem.accentor.media

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.SparseArray
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.Transformations.switchMap
import androidx.media2.common.MediaItem
import androidx.media2.common.MediaMetadata
import androidx.media2.common.SessionPlayer
import androidx.media2.session.MediaController
import androidx.media2.session.SessionCommand
import androidx.media2.session.SessionCommandGroup
import androidx.media2.session.SessionToken
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.data.AccentorDatabase
import me.vanpetegem.accentor.data.albums.Album
import me.vanpetegem.accentor.data.albums.AlbumDao
import me.vanpetegem.accentor.data.albums.AlbumRepository
import me.vanpetegem.accentor.data.authentication.AuthenticationDataSource
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository
import me.vanpetegem.accentor.data.tracks.Track
import me.vanpetegem.accentor.data.tracks.TrackDao
import me.vanpetegem.accentor.data.tracks.TrackRepository
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.concurrent.Executors

class MediaSessionConnection(application: Application) : AndroidViewModel(application) {

    private val authenticationDataSource = AuthenticationDataSource(application)

    private val mediaController: MediaController = MediaController.Builder(application)
        .setSessionToken(SessionToken(application, ComponentName(application, MusicService::class.java)))
        .setControllerCallback(ContextCompat.getMainExecutor(application), object : MediaController.ControllerCallback() {
            override fun onConnected(controller: MediaController, _allowedCommands: SessionCommandGroup) {
                onCurrentMediaItemChanged(controller, controller.currentMediaItem)
                _buffering.postValue(controller.bufferingState == SessionPlayer.BUFFERING_STATE_BUFFERING_AND_STARVED)
                onPlayerStateChanged(controller, controller.playerState)
                onPlaylistChanged(controller, controller.playlist, controller.playlistMetadata)
                onRepeatModeChanged(controller, controller.repeatMode)
                onShuffleModeChanged(controller, controller.shuffleMode)
            }

            override fun onCurrentMediaItemChanged(controller: MediaController, item: MediaItem?) {
                currentTrackId.postValue(item?.metadata?.getString(MediaMetadata.METADATA_KEY_MEDIA_ID)?.toInt())
                _queuePosition.postValue(controller.currentMediaItemIndex + 1)
            }

            override fun onBufferingStateChanged(controller: MediaController, item: MediaItem, state: Int) {
                when(state) {
                    SessionPlayer.BUFFERING_STATE_BUFFERING_AND_STARVED -> {
                        _buffering.postValue(true)
                    }
                    else -> {
                        _buffering.postValue(false)
                    }
                }
            }

            override fun onPlayerStateChanged(controller: MediaController, state: Int) {
                when (state) {
                    SessionPlayer.PLAYER_STATE_PLAYING -> {
                        _playing.postValue(true)
                    }
                    else -> {
                        _playing.postValue(false)
                    }
                }
                updateCurrentPosition()
            }

            override fun onPlaylistChanged(controller: MediaController, items: MutableList<MediaItem>?, metadata: MediaMetadata?) {
                _queue.postValue(items ?: ArrayList())
                onCurrentMediaItemChanged(controller, controller.currentMediaItem)
            }

            override fun onRepeatModeChanged(controller: MediaController, repeatMode: Int) =
                _repeatMode.postValue(repeatMode)

            override fun onShuffleModeChanged(controller: MediaController, shuffleMode: Int) =
                _shuffleMode.postValue(shuffleMode)

        }).build()

    private val currentTrackId = MutableLiveData<Int>().apply { postValue(null) }
    val currentTrack: LiveData<Track?> = switchMap(currentTrackId) { id ->
        id?.let { trackRepository.findById(id) }
    }
    val currentAlbum: LiveData<Album?> = switchMap(currentTrack) { t ->
        t?.let { albumRepository.findById(t.albumId) }
    }
    private val _currentPosition = MutableLiveData<Long>()
    val currentPosition: LiveData<Int> = map(_currentPosition) {
        (it / 1000).toInt()
    }

    private val _playing = MutableLiveData<Boolean>().apply { postValue(false) }
    val playing: LiveData<Boolean> = _playing

    private val _buffering = MutableLiveData<Boolean>().apply { postValue(false) }
    val buffering: LiveData<Boolean> = _buffering

    private val _repeatMode = MutableLiveData<Int>()
    val repeatMode: LiveData<Int> = _repeatMode

    private val _shuffleMode = MutableLiveData<Int>()
    val shuffleMode: LiveData<Int> = _shuffleMode

    private val _queue = MutableLiveData<List<MediaItem>>().apply { postValue(ArrayList()) }
    private val _queueIds: LiveData<List<Int>> = map(_queue) { it.map { item -> item.metadata?.mediaId!!.toInt() } }
    val queue: LiveData<List<Triple<Boolean, Track?, Album?>>>

    val _queuePosition: MutableLiveData<Int> = MutableLiveData<Int>().apply {
        postValue(0)
    }
    val queuePosition: LiveData<Int> = _queuePosition

    val queuePosStr: LiveData<String> = switchMap(_queue) { q ->
        map(queuePosition) {
            "$it/${q.size}"
        }
    }

    private val albumRepository: AlbumRepository;
    private val trackRepository: TrackRepository;

    init {
        val database = AccentorDatabase.getDatabase(application)
        val trackDao = database.trackDao()
        val albumDao = database.albumDao()
        albumRepository = AlbumRepository(albumDao, AuthenticationRepository(authenticationDataSource))
        trackRepository = TrackRepository(trackDao, AuthenticationRepository(authenticationDataSource))
        queue = switchMap(_queueIds) { q ->
            switchMap(queuePosition) { qPos ->
                switchMap(trackRepository.findByIds(q)) { tracks ->
                    map(albumRepository.findByIds(tracks.map { it.albumId })) { albums ->
                        q.mapIndexed { pos, id ->
                            val track = tracks.find { it.id == id }
                            val album = albums.find { it.id == track?.albumId }
                            Triple(qPos == pos + 1, track, album)
                        }
                    }
                }
            }

        }
    }

    fun stop() {
        mediaController.sendCustomCommand(SessionCommand("STOP", null), null)
    }

    fun play(tracks: List<Pair<Track, Album>>) {
        stop()
        mediaController.setPlaylist(tracks.map { it.first.id.toString() }, null)
        play()
    }

    fun play(album: Album) {
        doAsync {
            val tracks = trackRepository.getByAlbum(album).map { Pair(it, album) }
            uiThread { play(tracks) }
        }
    }

    fun addTracksToQueue(album: Album) = addTracksToQueue(album, _queue.value?.size ?: 0) {}

    fun addTracksToQueue(album: Album, index: Int) = addTracksToQueue(album, index) {}

    fun addTracksToQueue(album: Album, index: Int, resultHandler: () -> Unit) {
        doAsync {
            val tracks = trackRepository.getByAlbum(album).map { Pair(it, album) }
            uiThread {
                addTracksToQueue(tracks, index)
                resultHandler()
            }
        }
    }

    fun clearQueue() {
        stop()
        val size = _queue.value?.let { it -> it.size } ?: 0
        for (i in size downTo 0)
            removeFromQueue(i)
    }

    fun addTracksToQueue(tracks: List<Pair<Track, Album>>, index: Int) {
        var base = index
        tracks.forEach {
            mediaController.addPlaylistItem(base++, it.first.id.toString())
        }
    }

    fun previous() = mediaController.skipToPreviousPlaylistItem()

    fun pause() = mediaController.pause()

    fun play() = mediaController.play()

    fun next() = mediaController.skipToNextPlaylistItem()

    fun seekTo(time: Int) = mediaController.seekTo(time.toLong() * 1000)

    fun setRepeatMode(repeatMode: Int) = mediaController.setRepeatMode(repeatMode)

    fun setShuffleMode(shuffleMode: Int) = mediaController.setShuffleMode(shuffleMode)

    fun updateCurrentPosition() {
        if (mediaController.currentPosition == SessionPlayer.UNKNOWN_TIME)
            _currentPosition.postValue(0)
        else
            _currentPosition.postValue(mediaController.currentPosition)
    }

    fun skipTo(position: Int) = mediaController.skipToPlaylistItem(position)

    fun move(oldPosition: Int, newPosition: Int) =
        mediaController.movePlaylistItem(oldPosition, newPosition)

    fun removeFromQueue(position: Int) = mediaController.removePlaylistItem(position)

}
