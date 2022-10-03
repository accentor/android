package me.vanpetegem.accentor.media

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.Transformations.switchMap
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import me.vanpetegem.accentor.data.albums.Album
import me.vanpetegem.accentor.data.albums.AlbumRepository
import me.vanpetegem.accentor.data.authentication.AuthenticationDataSource
import me.vanpetegem.accentor.data.codecconversions.CodecConversionRepository
import me.vanpetegem.accentor.data.playlists.Playlist
import me.vanpetegem.accentor.data.preferences.PreferencesDataSource
import me.vanpetegem.accentor.data.tracks.Track
import me.vanpetegem.accentor.data.tracks.TrackRepository

@Singleton
class MediaSessionConnection @Inject constructor(
    private val application: Application,
    private val albumRepository: AlbumRepository,
    private val trackRepository: TrackRepository,
    private val preferencesDataSource: PreferencesDataSource,
    private val codecConversionRepository: CodecConversionRepository,
    private val authenticationDataSource: AuthenticationDataSource,
) {
    private val mainScope = MainScope()
    private val mediaControllerFuture: ListenableFuture<MediaController> =
        MediaController.Builder(
            application,
            SessionToken(application, ComponentName(application, MusicService::class.java))
        )
            .buildAsync()
            .apply { addListener({ setupController() }, MoreExecutors.directExecutor()) }

    private lateinit var mediaController: MediaController

    private val currentTrackId = MutableLiveData<Int>().apply { postValue(null) }
    val currentTrack: LiveData<Track?> =
        switchMap(currentTrackId) { id ->
            switchMap(_queue) { queue ->
                if (queue.size > 0) id?.let { trackRepository.findById(id) } else null
            }
        }
    val currentAlbum: LiveData<Album?> =
        switchMap(currentTrack) { t -> t?.let { albumRepository.findById(t.albumId) } }
    private val _currentPosition = MutableLiveData<Long>()
    val currentPosition: LiveData<Int> = map(_currentPosition) { (it / 1000).toInt() }

    private val _playing = MutableLiveData<Boolean>().apply { postValue(false) }
    val playing: LiveData<Boolean> = _playing

    private val _buffering = MutableLiveData<Boolean>().apply { postValue(false) }
    val buffering: LiveData<Boolean> = _buffering

    private val _repeatMode = MutableLiveData<Int>()
    val repeatMode: LiveData<Int> = _repeatMode

    private val _shuffleMode = MutableLiveData<Boolean>()
    val shuffleMode: LiveData<Boolean> = _shuffleMode

    private val _queue = MutableLiveData<List<MediaItem>>().apply { postValue(ArrayList()) }
    private val _queueIds: LiveData<List<Int>> =
        map(_queue) { it.map { item -> item.mediaId.toInt() } }
    val queue: LiveData<List<Triple<Boolean, Track?, Album?>>> =
        switchMap(_queueIds) { q ->
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

    val _queuePosition: MutableLiveData<Int> = MutableLiveData<Int>().apply { postValue(0) }
    val queueLength: LiveData<Int> = map(_queue) { it.size }
    val queuePosition: LiveData<Int> = _queuePosition

    val queuePosStr: LiveData<String> =
        switchMap(_queue) { q -> map(queuePosition) { "$it/${q.size}" } }

    fun setupController() {
        mediaController = mediaControllerFuture.get()

        val listener = object : Player.Listener {
            fun updateQueue() {
                currentTrackId.postValue(mediaController.currentMediaItem?.mediaId?.toInt())
                _queuePosition.postValue(mediaController.currentMediaItemIndex + 1)
                val list = ArrayList<MediaItem>()
                for (i in 0 until mediaController.mediaItemCount) {
                    list.add(mediaController.getMediaItemAt(i))
                }
                _queue.postValue(list)
            }

            override fun onMediaItemTransition(item: MediaItem?, reason: Int) = updateQueue()

            override fun onTimelineChanged(timeline: Timeline, reason: Int) = updateQueue()

            override fun onMediaMetadataChanged(metadata: MediaMetadata) = updateQueue()

            override fun onPlaybackStateChanged(playbackState: Int) {
                _buffering.postValue(playbackState == Player.STATE_BUFFERING)
                mainScope.launch(Main) { updateCurrentPosition() }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playing.postValue(isPlaying)
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                _repeatMode.postValue(repeatMode)
            }

            override fun onShuffleModeEnabledChanged(shuffle: Boolean) {
                _shuffleMode.postValue(shuffle)
            }
        }

        listener.onMediaItemTransition(mediaController.currentMediaItem, Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED)
        listener.onPlaybackStateChanged(mediaController.playbackState)
        listener.onIsPlayingChanged(mediaController.isPlaying)
        listener.onRepeatModeChanged(mediaController.repeatMode)
        listener.onShuffleModeEnabledChanged(mediaController.shuffleModeEnabled)

        mediaController.addListener(listener)
    }

    suspend fun stop() {
        mainScope.launch(Main) {
            mediaController.stop()
            clearQueue()
        }
    }

    suspend fun play(tracks: List<Pair<Track, Album>>) {
        mainScope.launch(Main) {
            stop()
            mediaController.setMediaItems(tracks.map { convertTrack(it.first) })
            play()
        }
    }

    suspend fun play(album: Album) {
        val tracks = trackRepository.getByAlbum(album).map { Pair(it, album) }
        play(tracks)
    }

    suspend fun play(playlist: Playlist) {
    }

    suspend fun play(track: Track) {
        val album = albumRepository.getById(track.albumId)
        album?.let { play(listOf(Pair(track, it))) }
    }

    suspend fun addTrackToQueue(track: Track): Unit = addTrackToQueue(track, _queue.value?.size ?: 0)
    suspend fun addTracksToQueue(album: Album): Unit = addTracksToQueue(album, _queue.value?.size ?: 0)
    suspend fun addTracksToQueue(playlist: Playlist): Unit = addTracksToQueue(playlist, _queue.value?.size ?: 0)

    suspend fun addTrackToQueue(track: Track, index: Int) {
        val album = albumRepository.getById(track.albumId)
        album?.let { addTracksToQueue(listOf(Pair(track, album)), index) }
    }

    suspend fun addTracksToQueue(album: Album, index: Int) {
        val tracks = trackRepository.getByAlbum(album).map { Pair(it, album) }
        addTracksToQueue(tracks, index)
    }

    suspend fun addTracksToQueue(playlist: Playlist, index: Int) {
    }

    suspend fun clearQueue() = mainScope.launch(Main) { mediaController.clearMediaItems() }

    suspend fun addTracksToQueue(tracks: List<Pair<Track, Album>>, index: Int) {
        mainScope.launch(Main) {
            mediaController.addMediaItems(index, tracks.map { convertTrack(it.first) })
        }
    }

    suspend fun previous() = mainScope.launch(Main) { mediaController.seekToPrevious() }

    suspend fun pause() = mainScope.launch(Main) { mediaController.pause() }

    suspend fun play() = mainScope.launch(Main) {
        mediaController.prepare()
        mediaController.play()
    }

    suspend fun next() = mainScope.launch(Main) { mediaController.seekToNext() }

    suspend fun seekTo(time: Int) = mainScope.launch(Main) { mediaController.seekTo(time.toLong() * 1000) }

    suspend fun setRepeatMode(repeatMode: Int) = mainScope.launch(Main) { mediaController.setRepeatMode(repeatMode) }

    suspend fun setShuffleMode(shuffleMode: Boolean) = mainScope.launch(Main) { mediaController.setShuffleModeEnabled(shuffleMode) }

    suspend fun updateCurrentPosition() = mainScope.launch(Main) {
        _currentPosition.postValue(mediaController.currentPosition)
    }

    suspend fun skipTo(position: Int) = mainScope.launch(Main) { mediaController.seekToDefaultPosition(position) }

    suspend fun removeFromQueue(position: Int) = mainScope.launch(Main) { mediaController.removeMediaItem(position) }

    private fun convertTrack(track: Track): MediaItem = MediaItem.Builder().setMediaId(track.id.toString()).build()
}
