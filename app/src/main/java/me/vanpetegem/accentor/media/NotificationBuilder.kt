package me.vanpetegem.accentor.media

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.support.v4.media.session.PlaybackStateCompat
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media2.common.MediaMetadata
import androidx.media2.common.SessionPlayer
import androidx.media2.session.MediaSession
import coil.imageLoader
import coil.request.ImageRequest
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.ui.main.MainActivity

const val NOW_PLAYING_CHANNEL: String = "me.vanpetegem.accentor.media.NOW_PLAYING_CHANNEL"
const val NOW_PLAYING_NOTIFICATION: Int = 0xb339

/**
 * Helper class to encapsulate code for building notifications.
 */
class NotificationBuilder(private val context: Context) {
    private val platformNotificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val skipToPreviousAction = NotificationCompat.Action(
        R.drawable.exo_controls_previous,
        context.getString(R.string.previous),
        createPendingIntent(PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
    )
    private val playAction = NotificationCompat.Action(
        R.drawable.exo_controls_play,
        context.getString(R.string.play),
        createPendingIntent(PlaybackStateCompat.ACTION_PLAY)
    )
    private val pauseAction = NotificationCompat.Action(
        R.drawable.exo_controls_pause,
        context.getString(R.string.pause),
        createPendingIntent(PlaybackStateCompat.ACTION_PAUSE)
    )
    private val skipToNextAction = NotificationCompat.Action(
        R.drawable.exo_controls_next,
        context.getString(R.string.play_next),
        createPendingIntent(PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
    )
    private val stopPendingIntent =
        createPendingIntent(PlaybackStateCompat.ACTION_STOP)

    suspend fun buildNotification(session: MediaSession): Notification {
        if (shouldCreateNowPlayingChannel()) {
            createNowPlayingChannel()
        }

        val player = session.player
        val metadata = player.currentMediaItem?.metadata
        val state = player.playerState

        val builder = NotificationCompat.Builder(context, NOW_PLAYING_CHANNEL)

        builder.addAction(skipToPreviousAction)
        if (state == SessionPlayer.PLAYER_STATE_PLAYING) {
            builder.addAction(pauseAction)
        } else {
            builder.addAction(playAction)
        }
        builder.addAction(skipToNextAction)

        val mediaStyle = MediaStyle()
            .setCancelButtonIntent(stopPendingIntent)
            .setMediaSession(session.getSessionCompatToken())
            .setShowActionsInCompactView(0, 1, 2)

        val openIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, openIntent, PendingIntent.FLAG_IMMUTABLE)

        val bitmap = metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI)?.let {
            context.imageLoader.execute(ImageRequest.Builder(context).data(it).build()).drawable?.toBitmap()
        }

        return builder.setContentIntent(pendingIntent)
            .setContentTitle(metadata?.getString(MediaMetadata.METADATA_KEY_TITLE))
            .setContentText(metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST))
            .setDeleteIntent(stopPendingIntent)
            .setLargeIcon(bitmap)
            .setOnlyAlertOnce(true)
            .setSmallIcon(R.drawable.ic_notification)
            .setStyle(mediaStyle)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun createPendingIntent(action: Long): PendingIntent {
        val keyCode = PlaybackStateCompat.toKeyCode(action)
        val intent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
            setComponent(ComponentName(context, context.javaClass))
            putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
        }
        if (action != PlaybackStateCompat.ACTION_PAUSE) {
            return PendingIntent.getForegroundService(context, keyCode, intent, 0)
        } else {
            return PendingIntent.getService(context, keyCode, intent, PendingIntent.FLAG_IMMUTABLE)
        }
    }

    private fun shouldCreateNowPlayingChannel() = !nowPlayingChannelExists()

    private fun nowPlayingChannelExists() =
        platformNotificationManager.getNotificationChannel(NOW_PLAYING_CHANNEL) != null

    private fun createNowPlayingChannel() {
        val notificationChannel = NotificationChannel(
            NOW_PLAYING_CHANNEL,
            context.getString(R.string.now_playing),
            NotificationManager.IMPORTANCE_LOW
        )
            .apply {
                description = context.getString(R.string.notification_channel_description)
                setShowBadge(false)
            }

        platformNotificationManager.createNotificationChannel(notificationChannel)
    }
}
