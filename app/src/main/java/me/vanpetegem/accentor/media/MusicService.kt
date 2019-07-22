package me.vanpetegem.accentor.media

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.PowerManager
import android.os.ResultReceiver
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.media.MediaBrowserServiceCompat
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSink
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import me.vanpetegem.accentor.data.tracks.Track
import me.vanpetegem.accentor.ui.main.MainActivity
import me.vanpetegem.accentor.userAgent
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File

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
        ExoPlayerFactory.newSimpleInstance(
            this,
            DefaultTrackSelector(),
            // TODO: This is ugly and should be done in a better way. See https://github.com/google/ExoPlayer/issues/6204
            DefaultLoadControl.Builder().setBufferDurationsMs(
                Int.MAX_VALUE,
                Int.MAX_VALUE,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
            ).createDefaultLoadControl()
        ).apply {
            setAudioAttributes(accentorAudioAttributes, true)
        }
    }

    override fun onCreate() {
        super.onCreate()

        mediaSession = MediaSessionCompat(baseContext, "MusicService").apply {
            setSessionActivity(packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                sessionIntent.flags = sessionIntent.flags or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                sessionIntent.putExtra(MainActivity.INTENT_EXTRA_OPEN_PLAYER, true)
                PendingIntent.getActivity(this@MusicService, 0, sessionIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            })
            isActive = true
        }
        sessionToken = mediaSession.sessionToken

        mediaController = MediaControllerCompat(this, mediaSession.sessionToken).also {
            it.registerCallback(MediaControllerCallback())
        }

        notificationBuilder = NotificationBuilder(this)
        notificationManager = NotificationManagerCompat.from(this)

        becomingNoisyReceiver = BecomingNoisyReceiver(this, mediaSession.sessionToken)

        val mediaSource = ConcatenatingMediaSource()
        mediaSessionConnector = MediaSessionConnector(mediaSession).also {
            it.setPlayer(exoPlayer)
            it.setQueueEditor(
                object : MediaSessionConnector.QueueEditor {
                    var count: Long = 0
                    val factory =
                        ProgressiveMediaSource.Factory(object : DataSource.Factory {
                            val base = DefaultDataSourceFactory(
                                this@MusicService.application,
                                DefaultHttpDataSourceFactory(userAgent, 0, 0, false)
                            )

                            val cache = SimpleCache(
                                File(this@MusicService.application.cacheDir, "audio"),
                                LeastRecentlyUsedCacheEvictor(10L * 1024L * 1024L * 1024L),
                                ExoDatabaseProvider(this@MusicService.application)
                            )

                            override fun createDataSource(): DataSource {
                                return CacheDataSource(
                                    cache,
                                    base.createDataSource(),
                                    FileDataSource(),
                                    CacheDataSink(cache, C.LENGTH_UNSET.toLong()),
                                    (CacheDataSource.FLAG_BLOCK_ON_CACHE or CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR),
                                    null
                                )
                            }
                        })

                    override fun onRemoveQueueItem(player: Player, description: MediaDescriptionCompat) {
                        for (i in 0..queue.size) {
                            if (queue[i].description.mediaId == description.mediaId) {
                                queue.removeAt(i)
                                mediaSession.setQueue(queue)
                                mediaSource.removeMediaSource(i)
                                return
                            }
                        }
                    }

                    override fun onAddQueueItem(player: Player, description: MediaDescriptionCompat) {
                        onAddQueueItem(player, description, queue.size)
                    }

                    override fun onAddQueueItem(player: Player, description: MediaDescriptionCompat, index: Int) {
                        mediaSource.addMediaSource(factory.createMediaSource(description.mediaUri))
                        queue.add(
                            index,
                            MediaSessionCompat.QueueItem(description, count++)
                        )
                        mediaSession.setQueue(queue)
                    }

                    override fun onCommand(
                        player: Player,
                        controlDispatcher: ControlDispatcher,
                        command: String,
                        extras: Bundle,
                        cb: ResultReceiver
                    ): Boolean = false
                })
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
            it.setMediaMetadataProvider { player ->
                val builder = MediaMetadataCompat.Builder()
                if (player.currentWindowIndex < queue.size) {
                    val item = queue[player.currentWindowIndex].description
                    val extras = item.extras!!
                    builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, item.mediaId)
                    builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, item.title.toString())
                    builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, item.subtitle.toString())
                    builder.putString(
                        MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST,
                        extras.getString(Track.ALBUMARTIST)
                    )
                    builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, extras.getString(Track.ARTIST))
                    builder.putString(MediaMetadataCompat.METADATA_KEY_DATE, extras.getString(Track.YEAR))
                    if (item.iconUri != null) {
                        try {
                            builder.putBitmap(
                                MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                                Glide.with(this@MusicService).load(item.iconUri).onlyRetrieveFromCache(true).submit().get().toBitmap()
                            )
                        } catch (e: Exception) {
                            doAsync {
                                Glide.with(this@MusicService).load(item.iconUri).preload()
                                uiThread {
                                    mediaSessionConnector.invalidateMediaSessionMetadata()
                                }
                            }
                        }
                    } else {

                    }
                }
                builder.build()
            }
            it.setQueueNavigator(object : TimelineQueueNavigator(mediaSession, Int.MAX_VALUE) {
                override fun getMediaDescription(player: Player?, windowIndex: Int): MediaDescriptionCompat =
                    queue[windowIndex].description
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

        val wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val wifiLock: WifiManager.WifiLock =
            wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "Accentor:WifiLock")
        val wakeLock: PowerManager.WakeLock =
            powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Accentor:WakeLock")

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            mediaController.playbackState?.let { updateNotification(it) }
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            state?.let { updateNotification(it) }
            state?.let { updateLocks(it) }
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

        @SuppressLint("WakelockTimeout")
        private fun updateLocks(state: PlaybackStateCompat) {

            when (state.state) {
                PlaybackStateCompat.STATE_BUFFERING, PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.STATE_CONNECTING -> {
                    if (!wakeLock.isHeld) wakeLock.acquire()
                    if (!wifiLock.isHeld) wifiLock.acquire()
                }
                else -> {
                    if (wakeLock.isHeld) wakeLock.release()
                    if (wifiLock.isHeld) wifiLock.release()
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