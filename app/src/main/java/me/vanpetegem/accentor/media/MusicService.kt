package me.vanpetegem.accentor.media

import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.media2.common.MediaItem
import androidx.media2.common.MediaMetadata
import androidx.media2.common.SessionPlayer
import androidx.media2.session.MediaSession
import androidx.media2.session.MediaSessionService
import androidx.media2.session.SessionCommand
import androidx.media2.session.SessionCommandGroup
import androidx.media2.session.SessionResult
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.ext.media2.SessionCallbackBuilder
import com.google.android.exoplayer2.ext.media2.SessionPlayerConnector
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.time.Instant
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.data.albums.Album
import me.vanpetegem.accentor.data.albums.AlbumRepository
import me.vanpetegem.accentor.data.authentication.AuthenticationDataSource
import me.vanpetegem.accentor.data.codecconversions.CodecConversionRepository
import me.vanpetegem.accentor.data.plays.PlayRepository
import me.vanpetegem.accentor.data.preferences.PreferencesDataSource
import me.vanpetegem.accentor.data.tracks.Track
import me.vanpetegem.accentor.data.tracks.TrackRepository
import me.vanpetegem.accentor.userAgent

@AndroidEntryPoint
class MusicService : MediaSessionService() {
    private val mainScope = MainScope()

    @Inject lateinit var authenticationDataSource: AuthenticationDataSource
    @Inject lateinit var preferencesDataSource: PreferencesDataSource
    @Inject lateinit var codecConversionRepository: CodecConversionRepository
    @Inject lateinit var trackRepository: TrackRepository
    @Inject lateinit var albumRepository: AlbumRepository
    @Inject lateinit var playRepository: PlayRepository

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

    private val baseDataSourceFactory by lazy {
        DefaultDataSource.Factory(this@MusicService.application, DefaultHttpDataSource.Factory().setUserAgent(userAgent))
    }
    private val cache: SimpleCache by lazy {
        SimpleCache(
            File(this@MusicService.application.cacheDir, "audio"),
            LeastRecentlyUsedCacheEvictor(preferencesDataSource.musicCacheSize.value!!),
            StandaloneDatabaseProvider(this@MusicService.application)
        )
    }

    private val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(this).apply {
            setMediaSourceFactory(
                ProgressiveMediaSource.Factory(
                    object : DataSource.Factory {
                        override fun createDataSource(): DataSource {
                            return CacheDataSource(
                                cache,
                                baseDataSourceFactory.createDataSource(),
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
            addListener(object : Player.Listener {
                private var trackId: Int? = null

                override fun onMediaItemTransition(item: com.google.android.exoplayer2.MediaItem?, reason: Int) {
                    trackId = item?.mediaId?.toInt()
                }

                override fun onPositionDiscontinuity(old: Player.PositionInfo, new: Player.PositionInfo, reason: Int) {
                    if (trackId != null && reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION) {
                        reportPlay()
                    }
                }

                override fun onPlaybackStateChanged(state: Int) {
                    if (trackId != null && state == Player.STATE_ENDED) {
                        reportPlay()
                        exoPlayer.stop()
                        exoPlayer.seekTo(0, 0)
                    }
                }

                private fun reportPlay() {
                    val savedTrackId = trackId!!
                    mainScope.launch(IO) { playRepository.reportPlay(savedTrackId, Instant.now()) }
                }
            })
        }
    }

    override fun onCreate() {
        super.onCreate()

        sessionPlayerConnector = SessionPlayerConnector(exoPlayer)
        sessionCallback = SessionCallbackBuilder(baseContext, sessionPlayerConnector).setMediaItemProvider(
            object : SessionCallbackBuilder.MediaItemProvider {
                override fun onCreateMediaItem(
                    session: MediaSession,
                    info: MediaSession.ControllerInfo,
                    mediaId: String
                ): MediaItem? {
                    val track = trackRepository.getById(mediaId.toInt())
                    val album = track?.let { albumRepository.getById(it.albumId) }
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
                        // We don't know the command. Shouldn't happen, but SUCCESS is the only value we're allowed to return.
                        else -> { return SessionResult(SessionResult.RESULT_SUCCESS, null) }
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
        val firstConversion by lazy { codecConversionRepository.getFirst() }
        val conversionParam = if (conversionId != null && codecConversionRepository.getById(conversionId) != null) {
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

        return MediaItem.Builder().setMetadata(builder.build()).build()
    }

    override fun onGetSession(info: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onUpdateNotification(session: MediaSession): MediaSessionService.MediaNotification? {
        mainScope.launch(IO) {
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
        }

        // Don't use automatic foregrounding/notification showing/etc...
        return null
    }
}
