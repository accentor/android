package me.vanpetegem.accentor.media

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueEditor
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory

class MusicService : MediaBrowserServiceCompat() {
    private lateinit var becomingNoisyReceiver: BecomingNoisyReceiver
    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var notificationBuilder: NotificationBuilder

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaController: MediaControllerCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    private var isForegroundService = false
    private val queue = ArrayList<MediaSessionCompat.QueueItem>()

    private val accentorAudioAttributes = AudioAttributes.Builder()
        .setContentType(C.CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    private val exoPlayer: ExoPlayer by lazy {
        ExoPlayerFactory.newSimpleInstance(this).apply {
            setAudioAttributes(accentorAudioAttributes, true)
        }
    }

    override fun onCreate() {
        super.onCreate()

        mediaSession = MediaSessionCompat(baseContext, "MusicService").apply {
            isActive = true
        }
        sessionToken = mediaSession.sessionToken

        mediaController = MediaControllerCompat(this, mediaSession).also {
            it.registerCallback(MediaControllerCallback())
        }

        notificationBuilder = NotificationBuilder(this)
        notificationManager = NotificationManagerCompat.from(this)

        becomingNoisyReceiver = BecomingNoisyReceiver(this, mediaSession.sessionToken)

        val mediaSource = ConcatenatingMediaSource()
        mediaSessionConnector = MediaSessionConnector(mediaSession).also {
            it.setPlayer(exoPlayer)
            it.setQueueEditor(
                TimelineQueueEditor(
                    mediaController,
                    mediaSource,
                    object : TimelineQueueEditor.QueueDataAdapter {
                        override fun add(position: Int, description: MediaDescriptionCompat) {
                            queue.add(
                                position,
                                MediaSessionCompat.QueueItem(description, description.mediaId!!.toLong())
                            )
                            mediaSession.setQueue(queue)
                            it.invalidateMediaSessionQueue()
                        }

                        override fun remove(position: Int) {
                            queue.removeAt(position)
                            mediaSession.setQueue(queue)
                            it.invalidateMediaSessionQueue()
                        }

                        override fun move(from: Int, to: Int) {
                            queue.add(if (to > from) to - 1 else 0, queue.removeAt(from))
                            mediaSession.setQueue(queue)
                            it.invalidateMediaSessionQueue()
                        }
                    },
                    object : TimelineQueueEditor.MediaSourceFactory {
                        val factory =
                            ProgressiveMediaSource.Factory(DefaultHttpDataSourceFactory("Accentor/${Build.VERSION.RELEASE}"))

                        override fun createMediaSource(description: MediaDescriptionCompat): MediaSource =
                            factory.createMediaSource(description.mediaUri)
                    })
            )
            it.setPlaybackPreparer(object : MediaSessionConnector.PlaybackPreparer {
                override fun onCommand(
                    player: Player?,
                    controlDispatcher: ControlDispatcher?,
                    command: String?,
                    extras: Bundle?,
                    cb: ResultReceiver?
                ): Boolean {
                    return false
                }

                override fun getSupportedPrepareActions(): Long =
                    PlaybackStateCompat.ACTION_PREPARE

                override fun onPrepareFromMediaId(mediaId: String?, extras: Bundle?) {
                }

                override fun onPrepareFromUri(uri: Uri?, extras: Bundle?) {
                }

                override fun onPrepareFromSearch(query: String?, extras: Bundle?) {
                }

                override fun onPrepare() {
                    exoPlayer.prepare(mediaSource)
                }
            })
        }

    }

    override fun onDestroy() {
        mediaSession.run {
            isActive = false
            release()
        }

        super.onDestroy()
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? =
        BrowserRoot("empty", null)

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) =
        result.sendResult(null)

    private fun removeNotification() = notificationManager.cancel(NOW_PLAYING_NOTIFICATION)

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            mediaController.playbackState?.let { updateNotification(it) }
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            state?.let { updateNotification(it) }
        }

        private fun updateNotification(state: PlaybackStateCompat) {
            val updatedState = state.state

            val notification = if (mediaController.metadata != null && updatedState != PlaybackStateCompat.STATE_NONE)
                notificationBuilder.buildNotification(mediaSession.sessionToken)
            else
                null

            when (updatedState) {
                PlaybackStateCompat.STATE_BUFFERING, PlaybackStateCompat.STATE_PLAYING -> {
                    becomingNoisyReceiver.register()

                    if (notification != null) {
                        notificationManager.notify(NOW_PLAYING_NOTIFICATION, notification)

                        if (!isForegroundService) {
                            ContextCompat.startForegroundService(
                                application,
                                Intent(application, this@MusicService.javaClass)
                            )
                            startForeground(NOW_PLAYING_NOTIFICATION, notification)
                            isForegroundService = true
                        }
                    }
                }
                else -> {
                    becomingNoisyReceiver.unregister()

                    if (isForegroundService) {
                        stopForeground(false)
                        isForegroundService = false

                        if (updatedState == PlaybackStateCompat.STATE_NONE) {
                            stopSelf()
                        }

                        if (notification != null) {
                            notificationManager.notify(NOW_PLAYING_NOTIFICATION, notification)
                        } else {
                            removeNotification()
                        }
                    }
                }
            }
        }
    }
}

private class BecomingNoisyReceiver(
    private val context: Context,
    sessionToken: MediaSessionCompat.Token
) : BroadcastReceiver() {

    private val noisyIntentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
    private val controller = MediaControllerCompat(context, sessionToken)

    private var registered = false

    fun register() {
        if (!registered) {
            context.registerReceiver(this, noisyIntentFilter)
            registered = true
        }
    }

    fun unregister() {
        if (registered) {
            context.unregisterReceiver(this)
            registered = false
        }
    }

    override fun onReceive(_context: Context, intent: Intent) {
        if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
            controller.transportControls.pause()
        }
    }

}