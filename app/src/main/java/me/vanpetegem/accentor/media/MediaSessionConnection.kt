package me.vanpetegem.accentor.media

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import me.vanpetegem.accentor.data.albums.Album
import me.vanpetegem.accentor.data.albums.AlbumRepository
import me.vanpetegem.accentor.data.playlists.Playlist
import me.vanpetegem.accentor.data.tracks.Track
import me.vanpetegem.accentor.data.tracks.TrackRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaSessionConnection
    @Inject
    constructor(
        application: Application,
        private val albumRepository: AlbumRepository,
        private val trackRepository: TrackRepository,
    ) {
        private val mainScope = MainScope()
        private val mediaControllerFuture: ListenableFuture<MediaController> =
            MediaController
                .Builder(
                    application,
                    SessionToken(application, ComponentName(application, MusicService::class.java)),
                ).buildAsync()
                .apply { addListener({ setupController() }, MoreExecutors.directExecutor()) }

        private lateinit var mediaController: MediaController

        private val currentTrackId = MutableLiveData<Int>().apply { postValue(null) }
        val currentTrack: LiveData<Track?> =
            currentTrackId.switchMap { id ->
                _queue.switchMap { queue ->
                    if (queue.isNotEmpty()) id?.let { trackRepository.findById(id) } else null
                }
            }
        val currentAlbum: LiveData<Album?> =
            currentTrack.switchMap { t -> t?.let { albumRepository.findById(t.albumId) } }
        private val _currentPosition = MutableLiveData<Long>()
        val currentPosition: LiveData<Int> = _currentPosition.map { (it / 1000).toInt() }

        private val _playing = MutableLiveData<Boolean>().apply { postValue(false) }
        val playing: LiveData<Boolean> = _playing

        private val _buffering = MutableLiveData<Boolean>().apply { postValue(false) }
        val buffering: LiveData<Boolean> = _buffering

        private val _repeatMode = MutableLiveData<Int>()
        val repeatMode: LiveData<Int> = _repeatMode

        private val _shuffleMode = MutableLiveData<Boolean>()
        val shuffleMode: LiveData<Boolean> = _shuffleMode

        private val _queue = MutableLiveData<List<MediaItem>>().apply { postValue(ArrayList()) }
        private val queueIds: LiveData<List<Int>> =
            _queue.map { it.map { item -> item.mediaId.toInt() } }
        val queue: LiveData<List<Triple<Boolean, Track?, Album?>>> =
            queueIds.switchMap { q ->
                queuePosition.switchMap { qPos ->
                    trackRepository.findByIds(q).switchMap { tracks ->
                        albumRepository.findByIds(tracks.map { it.albumId }).map { albums ->
                            q.mapIndexed { pos, id ->
                                val track = tracks.find { it.id == id }
                                val album = albums.find { it.id == track?.albumId }
                                Triple(qPos == pos + 1, track, album)
                            }
                        }
                    }
                }
            }

        private val _queuePosition: MutableLiveData<Int> = MutableLiveData<Int>().apply { postValue(0) }
        val queueLength: LiveData<Int> = _queue.map { it.size }
        val queuePosition: LiveData<Int> = _queuePosition

        val queuePosStr: LiveData<String> =
            _queue.switchMap { q -> queuePosition.map { "$it/${q.size}" } }

        fun setupController() {
            mediaController = mediaControllerFuture.get()

            val listener =
                object : Player.Listener {
                    fun updateQueue() {
                        currentTrackId.postValue(mediaController.currentMediaItem?.mediaId?.toInt())
                        _queuePosition.postValue(mediaController.currentMediaItemIndex + 1)
                        val list = ArrayList<MediaItem>()
                        for (i in 0 until mediaController.mediaItemCount) {
                            list.add(mediaController.getMediaItemAt(i))
                        }
                        _queue.postValue(list)
                    }

                    override fun onMediaItemTransition(
                        item: MediaItem?,
                        reason: Int,
                    ) = updateQueue()

                    override fun onTimelineChanged(
                        timeline: Timeline,
                        reason: Int,
                    ) = updateQueue()

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

        fun play(tracks: List<Pair<Track, Album>>) {
            mainScope.launch(Main) {
                mediaController.stop()
                mediaController.clearMediaItems()
                mediaController.setMediaItems(tracks.map { convertTrack(it.first) })
                mediaController.prepare()
                mediaController.play()
            }
        }

        fun play(album: Album) {
            val tracks = trackRepository.getByAlbum(album).map { Pair(it, album) }
            play(tracks)
        }

        fun play(playlist: Playlist) {
            val tracks = playlist.toTrackAlbumPairs(trackRepository, albumRepository)
            play(tracks)
        }

        fun play(track: Track) {
            val album = albumRepository.getById(track.albumId)
            album?.let { play(listOf(Pair(track, it))) }
        }

        fun addTrackToQueue(track: Track): Unit = addTrackToQueue(track, _queue.value?.size ?: 0)

        fun addTracksToQueue(album: Album): Unit = addTracksToQueue(album, _queue.value?.size ?: 0)

        fun addTracksToQueue(playlist: Playlist): Unit = addTracksToQueue(playlist, _queue.value?.size ?: 0)

        fun addTrackToQueue(
            track: Track,
            index: Int,
        ) {
            val album = albumRepository.getById(track.albumId)
            album?.let { addTracksToQueue(listOf(Pair(track, album)), index) }
        }

        fun addTracksToQueue(
            album: Album,
            index: Int,
        ) {
            val tracks = trackRepository.getByAlbum(album).map { Pair(it, album) }
            addTracksToQueue(tracks, index)
        }

        fun addTracksToQueue(
            playlist: Playlist,
            index: Int,
        ) {
            val tracks = playlist.toTrackAlbumPairs(trackRepository, albumRepository)
            addTracksToQueue(tracks, index)
        }

        fun clearQueue() = mainScope.launch(Main) { mediaController.clearMediaItems() }

        fun addTracksToQueue(
            tracks: List<Pair<Track, Album>>,
            index: Int,
        ) {
            mainScope.launch(Main) {
                mediaController.addMediaItems(index, tracks.map { convertTrack(it.first) })
            }
        }

        fun previous() = mainScope.launch(Main) { mediaController.seekToPrevious() }

        fun pause() = mainScope.launch(Main) { mediaController.pause() }

        fun play() =
            mainScope.launch(Main) {
                mediaController.prepare()
                mediaController.play()
            }

        fun next() = mainScope.launch(Main) { mediaController.seekToNext() }

        fun seekTo(time: Int) = mainScope.launch(Main) { mediaController.seekTo(time.toLong() * 1000) }

        fun setRepeatMode(repeatMode: Int) = mainScope.launch(Main) { mediaController.setRepeatMode(repeatMode) }

        fun setShuffleMode(shuffleMode: Boolean) = mainScope.launch(Main) { mediaController.setShuffleModeEnabled(shuffleMode) }

        fun updateCurrentPosition() =
            mainScope.launch(Main) {
                _currentPosition.postValue(mediaController.currentPosition)
            }

        fun skipTo(position: Int) = mainScope.launch(Main) { mediaController.seekToDefaultPosition(position) }

        fun removeFromQueue(position: Int) = mainScope.launch(Main) { mediaController.removeMediaItem(position) }

        private fun convertTrack(track: Track): MediaItem = MediaItem.Builder().setMediaId(track.id.toString()).build()
    }
