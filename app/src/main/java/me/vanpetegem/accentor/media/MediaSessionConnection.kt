package me.vanpetegem.accentor.media

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.net.Uri
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

class MediaSessionConnection(application: Application) : AndroidViewModel(application) {

    private val authenticationDataSource = AuthenticationDataSource(application)
    private val trackDao: TrackDao
    private val albumDao: AlbumDao

    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(application)
    private val mediaBrowser = MediaBrowserCompat(
        application,
        ComponentName(application, MusicService::class.java),
        mediaBrowserConnectionCallback,
        null
    ).apply { connect() }
    private val mediaDescBuilder = MediaDescriptionCompat.Builder()
    private lateinit var mediaController: MediaControllerCompat

    private val currentTrackId = MutableLiveData<Int>().apply { postValue(null) }
    val currentTrack: LiveData<Track?> = switchMap(currentTrackId) { id ->
        map(tracksById) { id?.let { id -> it[id] } }
    }
    val currentAlbum: LiveData<Album?> = switchMap(currentTrack) { t ->
        map(albumsById) { t?.let { t -> it[t.albumId] } }
    }

    private val _playing = MutableLiveData<Boolean>().apply { postValue(false) }
    val playing: LiveData<Boolean> = _playing

    private val _queue = MutableLiveData<List<Int>>().apply { postValue(ArrayList()) }
    val queue: LiveData<List<Track>> = switchMap(_queue) { q ->
        map(tracksById) { q.map { id -> it[id] } }
    }

    private val albumsById: LiveData<SparseArray<Album>>
    private val tracksById: LiveData<SparseArray<Track>>

    init {
        val database = AccentorDatabase.getDatabase(application)
        trackDao = database.trackDao()
        albumDao = database.albumDao()
        val albumRepository = AlbumRepository(albumDao, AuthenticationRepository(authenticationDataSource))
        albumsById = albumRepository.allAlbumsById
        val trackRepository = TrackRepository(trackDao, AuthenticationRepository(authenticationDataSource))
        tracksById = trackRepository.allTracksById
    }

    private fun convertTrack(track: Track): MediaDescriptionCompat {
        val album = albumDao.getAlbumById(track.albumId)
        val mediaUri =
            ("${authenticationDataSource.getServer()}/api/tracks/${track.id}/audio" +
                    "?secret=${authenticationDataSource.getSecret()}" +
                    "&device_id=${authenticationDataSource.getDeviceId()}" +
                    "&codec_conversion_id=1").toUri()

        return mediaDescBuilder
            .setTitle(track.title)
            .setSubtitle(album?.title ?: "")
            .setIconUri(album?.image?.toUri())
            .setMediaUri(mediaUri)
            .setMediaId(track.id.toString())
            .build()
    }

    fun play(tracks: List<Track>) {
        doAsync {
            mediaController.transportControls.stop()
            mediaController.queue?.forEach {
                mediaController.removeQueueItem(it.description)
            }
            var base = 0
            tracks.forEach {
                mediaController.addQueueItem(convertTrack(it), base++)
            }
            if (tracks.isNotEmpty()) {
                mediaController.transportControls.play()
            }
        }
    }

    fun previous() {
        mediaController.transportControls.skipToPrevious()
    }

    fun pause() {
        mediaController.transportControls.pause()
    }

    fun play() {
        mediaController.transportControls.play()
    }

    fun next() {
        mediaController.transportControls.skipToNext()
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
            }
        }
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            currentTrackId.postValue(metadata?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)?.toInt())
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            when (state?.state) {
                PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.STATE_BUFFERING -> _playing.postValue(true)
                else -> _playing.postValue(false)
            }
        }

        override fun onQueueChanged(queue: MutableList<MediaSessionCompat.QueueItem>?) {
            if (queue == null) {
                _queue.postValue(null)
                return
            }
            doAsync {
                _queue.postValue(queue.map { it.description.mediaId!!.toInt() })
            }
        }
    }
}

fun String?.toUri(): Uri = this?.let { Uri.parse(it) } ?: Uri.EMPTY
