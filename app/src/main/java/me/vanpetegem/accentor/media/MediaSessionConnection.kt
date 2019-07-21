package me.vanpetegem.accentor.media

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import me.vanpetegem.accentor.data.AccentorDatabase
import me.vanpetegem.accentor.data.albums.Album
import me.vanpetegem.accentor.data.albums.AlbumDao
import me.vanpetegem.accentor.data.authentication.AuthenticationDataSource
import me.vanpetegem.accentor.data.tracks.Track
import me.vanpetegem.accentor.data.tracks.TrackDao
import org.jetbrains.anko.doAsync

class MediaSessionConnection(application: Application) : AndroidViewModel(application) {

    private val authenticationDataSource = AuthenticationDataSource(application)
    private val trackDao: TrackDao
    private val albumDao: AlbumDao

    private val _connected = MutableLiveData<Boolean>().apply { postValue(false) }
    val connected: LiveData<Boolean> = _connected

    private val _networkFailure = MutableLiveData<Boolean>().apply { postValue(false) }
    val networkFailure: LiveData<Boolean> = _networkFailure

    private val _currentTrack = MutableLiveData<Track>().apply { postValue(null) }
    val currentTrack: LiveData<Track> = _currentTrack

    private val _currentAlbum = MutableLiveData<Album>().apply { postValue(null) }
    val currentAlbum: LiveData<Album> = _currentAlbum

    private val _playing = MutableLiveData<Boolean>().apply { postValue(false) }
    val playing: LiveData<Boolean> = _playing

    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(application)
    private val mediaBrowser = MediaBrowserCompat(
        application,
        ComponentName(application, MusicService::class.java),
        mediaBrowserConnectionCallback,
        null
    ).apply { connect() }
    private val mediaDescBuilder = MediaDescriptionCompat.Builder()

    lateinit var mediaController: MediaControllerCompat

    init {
        val database = AccentorDatabase.getDatabase(application)
        trackDao = database.trackDao()
        albumDao = database.albumDao()
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

    private fun onMetadataChanged(metadata: MediaMetadataCompat?) {
        if (metadata == null) {
            _currentTrack.postValue(null)
            _currentAlbum.postValue(null)
            return
        }
        doAsync {
            val track = trackDao.getTrackById(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID).toInt())
            _currentTrack.postValue(track)
            _currentAlbum.postValue(track?.let { albumDao.getAlbumById(it.albumId) })
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
                registerCallback(MediaControllerCallback())
                onMetadataChanged(metadata)
            }

            _connected.postValue(true)
        }

        override fun onConnectionSuspended() = _connected.postValue(false)

        override fun onConnectionFailed() = _connected.postValue(false)
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            this@MediaSessionConnection.onMetadataChanged(metadata)
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            when (state?.state) {
                PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.STATE_BUFFERING -> _playing.postValue(true)
                else -> _playing.postValue(false)
            }
        }
    }
}

fun String?.toUri(): Uri = this?.let { Uri.parse(it) } ?: Uri.EMPTY
