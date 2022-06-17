package me.vanpetegem.accentor.media

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.guava.future
import kotlinx.coroutines.launch
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.data.albums.AlbumRepository
import me.vanpetegem.accentor.data.authentication.AuthenticationDataSource
import me.vanpetegem.accentor.data.codecconversions.CodecConversionRepository
import me.vanpetegem.accentor.data.plays.PlayRepository
import me.vanpetegem.accentor.data.preferences.PreferencesDataSource
import me.vanpetegem.accentor.data.tracks.TrackRepository
import me.vanpetegem.accentor.ui.main.MainActivity
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

    private lateinit var mediaSession: MediaSession

    private val accentorAudioAttributes = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
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
        ExoPlayer.Builder(this)
            .setMediaSourceFactory(
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
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .setHandleAudioBecomingNoisy(true)
            .setAudioAttributes(accentorAudioAttributes, true)
            .build().apply {
                addListener(object : Player.Listener {
                    private var trackId: Int? = null

                    override fun onMediaItemTransition(item: MediaItem?, reason: Int) {
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

        val openIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, openIntent, PendingIntent.FLAG_IMMUTABLE)

        mediaSession = MediaSession.Builder(baseContext, exoPlayer)
            .setSessionActivity(pendingIntent)
            .setCallback(object : MediaSession.Callback {
                override fun onAddMediaItems(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo,
                    mediaItems: List<MediaItem>
                ): ListenableFuture<List<MediaItem>> {
                    return mainScope.future(IO) { convertTracks(mediaItems) }
                }
            })
            .build()

        val notificationBuilder = NotificationBuilder(this, mainScope)
        setMediaNotificationProvider(object : MediaNotification.Provider {
            override fun createNotification(
                session: MediaSession,
                customLayout: ImmutableList<CommandButton>,
                actionFactory: MediaNotification.ActionFactory,
                onNotificationChangedCallback: MediaNotification.Provider.Callback,
            ): MediaNotification =
                notificationBuilder.buildNotification(session, actionFactory, onNotificationChangedCallback)

            // Ignore, there are none.
            override fun handleCustomCommand(session: MediaSession, action: String, extras: Bundle): Boolean = false
        })
    }

    override fun onGetSession(info: MediaSession.ControllerInfo): MediaSession? = mediaSession

    private suspend fun convertTracks(items: List<MediaItem>): List<MediaItem> {
        val converted = items.map { convertTrack(it.mediaId.toInt()) }
        val filtered = converted.filterNotNull()
        return filtered
    }

    private suspend fun convertTrack(id: Int): MediaItem? {
        val track = trackRepository.getById(id) ?: return null
        val album = track.let { albumRepository.getById(it.albumId) } ?: return null

        val conversionId = preferencesDataSource.conversionId.value
        val firstConversion by lazy { codecConversionRepository.getFirst() }
        val conversion = conversionId?.let { codecConversionRepository.getById(conversionId) } ?: firstConversion
        val conversionParam = conversion?.let { "&codec_conversion_id=${it.id}" } ?: ""
        val mediaUri = "${authenticationDataSource.getServer()}/api/tracks/${track.id}/audio" +
            "?secret=${authenticationDataSource.getSecret()}" +
            "&device_id=${authenticationDataSource.getDeviceId()}" +
            conversionParam

        val metadata = MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.stringifyTrackArtists())
            .setAlbumTitle(album.title)
            .setAlbumArtist(album.stringifyAlbumArtists().let { if (it.isEmpty()) application.getString(R.string.various_artists) else it })
            .setArtworkUri(Uri.parse(album.image500))
            .setTrackNumber(track.number)
            .setReleaseYear(album.release.year)
            .setReleaseMonth(album.release.monthValue)
            .setReleaseDay(album.release.dayOfMonth)
            .build()

        return MediaItem.Builder()
            .setMediaId(track.id.toString())
            .setMediaMetadata(metadata)
            .setUri(mediaUri)
            .build()
    }
}
