package me.vanpetegem.accentor.media

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.media.session.MediaSession
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.SparseArray
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.Transformations.switchMap
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
import me.vanpetegem.accentor.media.extensions.currentPlayBackPosition

class MediaSessionConnection(application: Application) : AndroidViewModel(application) {

    private val authenticationDataSource = AuthenticationDataSource(application)

    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(application)
    private val mediaBrowser = MediaBrowserCompat(
        application,
        ComponentName(application, MusicService::class.java),
        mediaBrowserConnectionCallback,
        null
    ).apply { connect() }
    private val mediaDescBuilder = MediaDescriptionCompat.Builder()
    private var mediaController: MediaControllerCompat? = null

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

    private val _queue = MutableLiveData<List<MediaSessionCompat.QueueItem>>().apply { postValue(ArrayList()) }
    private val _queueIds: LiveData<List<Int>> = map(_queue) { it.map { item -> item.description.mediaId!!.toInt() } }
    val queue: LiveData<List<Triple<Boolean, Track?, Album?>>>

    private val activeQueueItemId = MutableLiveData<Long>().apply {
        postValue(MediaSession.QueueItem.UNKNOWN_ID.toLong())
    }

    val queuePosition: LiveData<Int> = switchMap(_queue) { q ->
        map(activeQueueItemId) {
            q.indexOfFirst { item -> item.queueId == it } + 1
        }
    }

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

    private fun convertTrack(track: Track, album: Album): MediaDescriptionCompat {
        val mediaUri =
            ("${authenticationDataSource.getServer()}/api/tracks/${track.id}/audio" +
                    "?secret=${authenticationDataSource.getSecret()}" +
                    "&device_id=${authenticationDataSource.getDeviceId()}" +
                    "&codec_conversion_id=4").toUri()

        val extras = Bundle()
        extras.putString(Track.ALBUMARTIST, album.stringifyAlbumArtists().let {
            if (it.isEmpty())
                this.getApplication<Application>().getString(R.string.various_artists)
            else
                it
        })
        extras.putString(Track.ARTIST, track.stringifyTrackArtists())
        extras.putString(Track.YEAR, album.release.toString())

        return mediaDescBuilder
            .setTitle(track.title)
            .setSubtitle(album.title)
            .setIconUri(album.image500?.toUri())
            .setMediaUri(mediaUri)
            .setMediaId(track.id.toString())
            .setExtras(extras)
            .build()
    }

    fun play(tracks: List<Pair<Track, Album>>) {
        stop()
        clearQueue()
        addTracksToQueue(tracks, 0)
        play()
    }

    fun clearQueue() = mediaController?.queue?.forEach { mediaController?.removeQueueItem(it.description) }

    fun addTracksToQueue(tracks: List<Pair<Track, Album>>) = addTracksToQueue(tracks, _queue.value?.size ?: 0)

    fun addTracksToQueue(tracks: List<Pair<Track, Album>>, index: Int) {
        var base = index
        tracks.forEach {
            mediaController?.addQueueItem(convertTrack(it.first, it.second), base++)
        }
    }

    fun previous() = mediaController?.transportControls?.skipToPrevious()


    fun pause() = mediaController?.transportControls?.pause()


    fun play() = mediaController?.transportControls?.play()


    fun stop() = mediaController?.transportControls?.stop()


    fun next() = mediaController?.transportControls?.skipToNext()


    fun seekTo(time: Int) = mediaController?.transportControls?.seekTo(time.toLong() * 1000)


    fun setRepeatMode(repeatMode: Int) = mediaController?.transportControls?.setRepeatMode(repeatMode)


    fun setShuffleMode(shuffleMode: Int) = mediaController?.transportControls?.setShuffleMode(shuffleMode)

    fun updateCurrentPosition() {
        mediaController?.playbackState?.currentPlayBackPosition?.let { _currentPosition.postValue(it) }
    }

    fun skipTo(track: Track) {
        _queue.value?.find { it.description.mediaId == track.id.toString() }?.queueId?.let {
            mediaController?.transportControls?.skipToQueueItem(it)
        }
    }

    fun move(track: Track, newPosition: Int) {
        val oldPosition = _queue.value?.indexOfFirst { it.description.mediaId == track.id.toString() } ?: return

        val argsBundle = Bundle()
        argsBundle.putInt(MusicService.MOVE_COMMAND_FROM, oldPosition)
        argsBundle.putInt(MusicService.MOVE_COMMAND_TO, newPosition)
        mediaController?.sendCommand(MusicService.MOVE_COMMAND, argsBundle, null)
    }

    fun removeFromQueue(track: Track) {
        _queue.value?.find { it.description.mediaId == track.id.toString() }?.description?.let {
            mediaController?.removeQueueItem(it)
        }
    }

    private inner class MediaBrowserConnectionCallback(private val context: Context) :
        MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                val callback = MediaControllerCallback()
                registerCallback(callback)
                callback.onMetadataChanged(metadata)
                callback.onPlaybackStateChanged(playbackState)
                callback.onQueueChanged(queue)
                callback.onRepeatModeChanged(repeatMode)
                callback.onShuffleModeChanged(shuffleMode)
            }
        }
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            currentTrackId.postValue(metadata?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)?.toInt())
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            when (state?.state) {
                PlaybackStateCompat.STATE_PLAYING -> {
                    _playing.postValue(true)
                    _buffering.postValue(false)
                }
                PlaybackStateCompat.STATE_BUFFERING -> {
                    _playing.postValue(true)
                    _buffering.postValue(true)
                }
                else -> {
                    _playing.postValue(false)
                    _buffering.postValue(false)
                }
            }

            state?.activeQueueItemId?.let { activeQueueItemId.postValue(it) }
            state?.currentPlayBackPosition?.let { _currentPosition.postValue(it) }
        }

        override fun onQueueChanged(queue: MutableList<MediaSessionCompat.QueueItem>?) {
            _queue.postValue(queue ?: ArrayList())
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            _repeatMode.postValue(repeatMode)
        }

        override fun onShuffleModeChanged(shuffleMode: Int) {
            _shuffleMode.postValue(shuffleMode)
        }
    }
}

fun String?.toUri(): Uri = this?.let { Uri.parse(it) } ?: Uri.EMPTY
