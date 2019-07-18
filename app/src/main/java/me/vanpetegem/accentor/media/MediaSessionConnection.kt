package me.vanpetegem.accentor.media

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaControllerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import me.vanpetegem.accentor.data.AccentorDatabase
import me.vanpetegem.accentor.data.albums.AlbumDao
import me.vanpetegem.accentor.data.authentication.AuthenticationDataSource
import me.vanpetegem.accentor.data.tracks.Track
import me.vanpetegem.accentor.data.tracks.TrackDao
import org.jetbrains.anko.doAsync

class MediaSessionConnection(application: Application) : AndroidViewModel(application) {

    private val authenticationDataSource = AuthenticationDataSource(application)
    private val trackDao: TrackDao
    private val albumDao: AlbumDao

    val isConnected = MutableLiveData<Boolean>().apply { postValue(false) }
    val networkFailure = MutableLiveData<Boolean>().apply { postValue(false) }

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
            .setIconUri(album?.image.toUri())
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
            tracks.forEach {
                mediaController.addQueueItem(convertTrack(it))
            }
            if (tracks.isNotEmpty()) {
                mediaController.transportControls.play()
            }
        }
    }

    private inner class MediaBrowserConnectionCallback(private val context: Context) :
        MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                registerCallback(MediaControllerCallback())
            }

            isConnected.postValue(true)
        }

        override fun onConnectionSuspended() =
            isConnected.postValue(false)

        override fun onConnectionFailed() = isConnected.postValue(false)
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback()
}

fun String?.toUri(): Uri = this?.let { Uri.parse(it) } ?: Uri.EMPTY
