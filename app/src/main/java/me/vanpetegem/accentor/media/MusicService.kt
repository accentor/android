package me.vanpetegem.accentor.media

import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.media2.common.MediaItem
import androidx.media2.common.MediaMetadata
import androidx.media2.common.SessionPlayer
import androidx.media2.session.MediaSession
import androidx.media2.session.MediaSessionService
import androidx.media2.session.SessionCommand
import androidx.media2.session.SessionCommandGroup
import androidx.media2.session.SessionResult
import coil.executeBlocking
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.ext.media2.SessionCallbackBuilder
import com.google.android.exoplayer2.ext.media2.SessionPlayerConnector
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import java.io.File
import java.util.concurrent.Executors
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.data.AccentorDatabase
import me.vanpetegem.accentor.data.albums.Album
import me.vanpetegem.accentor.data.authentication.AuthenticationDataSource
import me.vanpetegem.accentor.data.codecconversions.CodecConversionDao
import me.vanpetegem.accentor.data.preferences.PreferencesDataSource
import me.vanpetegem.accentor.data.tracks.Track
import me.vanpetegem.accentor.userAgent

class MusicService : MediaSessionService() {
    private val mainScope = MainScope()

    private lateinit var authenticationDataSource: AuthenticationDataSource
    private lateinit var preferencesDataSource: PreferencesDataSource
    private lateinit var codecConversionDao: CodecConversionDao

    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var notificationBuilder: NotificationBuilder

    private lateinit var mediaSession: MediaSession
    private lateinit var sessionCallback: MediaSession.SessionCallback
    private lateinit var sessionPlayerConnector: SessionPlayerConnector

    private var isForegroudService = false

    private val accentorAudioAttributes = AudioAttributes.Builder()
        .setContentType(C.CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    private val exoPlayer: ExoPlayer by lazy {
        SimpleExoPlayer.Builder(this).apply {
            setMediaSourceFactory(
                ProgressiveMediaSource.Factory(
                    object : DataSource.Factory {
                        val base = DefaultDataSourceFactory(this@MusicService.application, DefaultHttpDataSource.Factory().setUserAgent(userAgent))
                        val cache = SimpleCache(
                            File(this@MusicService.application.cacheDir, "audio"),
                            LeastRecentlyUsedCacheEvictor(preferencesDataSource.musicCacheSize.value!!),
                            ExoDatabaseProvider(this@MusicService.application)
                        )

                        override fun createDataSource(): DataSource {
                            return CacheDataSource(
                                cache,
                                base.createDataSource(),
                                (CacheDataSource.FLAG_BLOCK_ON_CACHE or CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
                            )
                        }
                    },
                    DefaultExtractorsFactory().setConstantBitrateSeekingEnabled(true)
                )
            )
            setHandleAudioBecomingNoisy(true)
        }.build().apply {
            setAudioAttributes(accentorAudioAttributes, true)
        }
    }

    override fun onCreate() {
        super.onCreate()

        authenticationDataSource = AuthenticationDataSource(application)
        preferencesDataSource = PreferencesDataSource(application)
        val database = AccentorDatabase.getDatabase(application)
        val trackDao = database.trackDao()
        val albumDao = database.albumDao()
        codecConversionDao = database.codecConversionDao()

        sessionPlayerConnector = SessionPlayerConnector(exoPlayer)
        sessionCallback = SessionCallbackBuilder(baseContext, sessionPlayerConnector).setMediaItemProvider(
            object : SessionCallbackBuilder.MediaItemProvider {
                override fun onCreateMediaItem(
                    session: MediaSession,
                    info: MediaSession.ControllerInfo,
                    mediaId: String
                ): MediaItem? {
                    val track = trackDao.getTrackById(mediaId.toInt())
                    val album = track?.let { albumDao.getAlbumById(it.albumId) }
                    return track?.let { t -> album?.let { a -> convertTrack(t, a) } }
                }
            }
        ).setCustomCommandProvider(
            object : SessionCallbackBuilder.CustomCommandProvider {
                override fun onCustomCommand(
                    session: MediaSession,
                    info: MediaSession.ControllerInfo,
                    command: SessionCommand,
                    args: Bundle?
                ): SessionResult {
                    when (command.customAction) {
                        "STOP" -> {
                            mainScope.launch(Main) { exoPlayer.stop() }
                            return SessionResult(SessionResult.RESULT_SUCCESS, null)
                        }
                        "CLEAR" -> {
                            mainScope.launch(Main) {
                                exoPlayer.stop()
                                exoPlayer.clearMediaItems()
                            }
                            return SessionResult(SessionResult.RESULT_SUCCESS, null)
                        }
                        else -> { return SessionResult(SessionResult.RESULT_ERROR_UNKNOWN, null) }
                    }
                }

                override fun getCustomCommands(
                    session: MediaSession,
                    info: MediaSession.ControllerInfo
                ): SessionCommandGroup? {
                    return SessionCommandGroup.Builder()
                        .addCommand(SessionCommand("STOP", null))
                        .addCommand(SessionCommand("CLEAR", null))
                        .build()
                }
            }
        ).setAllowedCommandProvider(
            object : SessionCallbackBuilder.AllowedCommandProvider by SessionCallbackBuilder.DefaultAllowedCommandProvider(this@MusicService) {
                override fun getAllowedCommands(
                    session: MediaSession,
                    controllerInfo: MediaSession.ControllerInfo,
                    base: SessionCommandGroup
                ): SessionCommandGroup {
                    // This is a work-around. ExoPlayer reports that previous is not possible on the first track, because it only looks at if there is a
                    // previous track, even though previous item skips back to the start of the track after three seconds. I tried to take these three seconds
                    // into account, but this method isn't called often enough.
                    if (!base.hasCommand(SessionCommand(SessionCommand.COMMAND_CODE_PLAYER_SKIP_TO_PREVIOUS_PLAYLIST_ITEM))) {
                        val builder = SessionCommandGroup.Builder()
                        for (command in base.getCommands()) {
                            builder.addCommand(command)
                        }
                        builder.addCommand(SessionCommand(SessionCommand.COMMAND_CODE_PLAYER_SKIP_TO_PREVIOUS_PLAYLIST_ITEM))
                        return builder.build()
                    } else {
                        return base
                    }
                }
            }
        ).build()
        mediaSession = MediaSession.Builder(baseContext, sessionPlayerConnector)
            .setSessionCallback(Executors.newSingleThreadExecutor(), sessionCallback)
            .build()

        notificationManager = NotificationManagerCompat.from(this)
        notificationBuilder = NotificationBuilder(this)
    }

    private fun convertTrack(track: Track, album: Album): MediaItem {
        val conversionId = preferencesDataSource.conversionId.value
        val firstConversion by lazy { codecConversionDao.getFirstCodecConversion() }
        val conversionParam = if (conversionId != null && codecConversionDao.getCodecConversionById(conversionId) != null) {
            "&codec_conversion_id=$conversionId"
        } else if (firstConversion != null) {
            "&codec_conversion_id=${firstConversion!!.id}"
        } else {
            ""
        }
        val mediaUri = "${authenticationDataSource.getServer()}/api/tracks/${track.id}/audio" +
            "?secret=${authenticationDataSource.getSecret()}" +
            "&device_id=${authenticationDataSource.getDeviceId()}" +
            conversionParam

        val builder = MediaMetadata.Builder()
        builder.putString(MediaMetadata.METADATA_KEY_TITLE, track.title)
        builder.putString(MediaMetadata.METADATA_KEY_ALBUM, album.title)
        builder.putString(MediaMetadata.METADATA_KEY_ARTIST, track.stringifyTrackArtists())
        builder.putString(MediaMetadata.METADATA_KEY_DATE, album.release.toString())
        builder.putString(
            MediaMetadata.METADATA_KEY_ALBUM_ARTIST,
            album.stringifyAlbumArtists().let {
                if (it.isEmpty()) application.getString(R.string.various_artists) else it
            }
        )
        builder.putString(MediaMetadata.METADATA_KEY_ART_URI, album.image500)
        builder.putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, album.image500)
        builder.putString(MediaMetadata.METADATA_KEY_MEDIA_URI, mediaUri)
        builder.putString(MediaMetadata.METADATA_KEY_MEDIA_ID, track.id.toString())

        if (album.image500 != null) {
            val bitmap = (this@MusicService).imageLoader.executeBlocking(
                ImageRequest.Builder(this@MusicService).data(album.image500).networkCachePolicy(CachePolicy.DISABLED).build()
            ).drawable?.toBitmap()
            if (bitmap != null) {
                builder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, bitmap)
                builder.putBitmap(MediaMetadata.METADATA_KEY_ART, bitmap)
            }
        }

        return MediaItem.Builder().setMetadata(builder.build()).build()
    }

    override fun onGetSession(info: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onUpdateNotification(session: MediaSession): MediaSessionService.MediaNotification? {
        val notification = if (session.player.currentMediaItem?.metadata != null) {
            notificationBuilder.buildNotification(session)
        } else { null }

        when (session.player.playerState) {
            SessionPlayer.PLAYER_STATE_PLAYING -> {
                if (notification != null) {
                    notificationManager.notify(NOW_PLAYING_NOTIFICATION, notification)
                    if (!isForegroudService) {
                        ContextCompat.startForegroundService(application, Intent(application, this.javaClass))
                        startForeground(NOW_PLAYING_NOTIFICATION, notification)
                        isForegroudService = true
                    }
                }
            }
            else -> {
                if (isForegroudService) {
                    stopForeground(false)
                    isForegroudService = false

                    if (session.player.playerState == SessionPlayer.PLAYER_STATE_IDLE) {
                        stopSelf()
                    }

                    if (notification != null) {
                        notificationManager.notify(NOW_PLAYING_NOTIFICATION, notification)
                    } else {
                        notificationManager.cancel(NOW_PLAYING_NOTIFICATION)
                    }
                }
            }
        }

        // Don't use automatic foregrounding/notification showing/etc...
        return null
    }
}
