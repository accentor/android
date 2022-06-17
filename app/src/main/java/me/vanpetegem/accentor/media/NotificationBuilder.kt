package me.vanpetegem.accentor.media

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media3.common.Player
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import coil.imageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.ui.main.MainActivity

const val NOW_PLAYING_CHANNEL: String = "me.vanpetegem.accentor.media.NOW_PLAYING_CHANNEL"
const val NOW_PLAYING_NOTIFICATION: Int = 0xb339

/**
 * Helper class to encapsulate code for building notifications.
 */
class NotificationBuilder(private val context: Context, private val scope: CoroutineScope) {
    private val platformNotificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun buildNotification(
        session: MediaSession,
        actionFactory: MediaNotification.ActionFactory,
        onNotificationChangedCallback: MediaNotification.Provider.Callback,
    ): MediaNotification {
        if (shouldCreateNowPlayingChannel()) {
            createNowPlayingChannel()
        }

        val player = session.player
        val metadata = player.currentMediaItem?.mediaMetadata

        val builder = NotificationCompat.Builder(context, NOW_PLAYING_CHANNEL)

        val previousAction = actionFactory.createMediaAction(
            session,
            IconCompat.createWithResource(context, R.drawable.ic_previous),
            context.getString(R.string.previous),
            Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM,
        )
        val pauseAction = actionFactory.createMediaAction(
            session,
            IconCompat.createWithResource(context, R.drawable.ic_pause),
            context.getString(R.string.pause),
            Player.COMMAND_PLAY_PAUSE,
        )
        val playAction = actionFactory.createMediaAction(
            session,
            IconCompat.createWithResource(context, R.drawable.ic_play),
            context.getString(R.string.play),
            Player.COMMAND_PLAY_PAUSE,
        )
        val nextAction = actionFactory.createMediaAction(
            session,
            IconCompat.createWithResource(context, R.drawable.ic_next),
            context.getString(R.string.next),
            Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
        )
        val stopPendingIntent = actionFactory.createMediaActionPendingIntent(
            session,
            Player.COMMAND_STOP.toLong()
        )

        builder.addAction(previousAction)
        if (player.isPlaying) {
            builder.addAction(pauseAction)
        } else {
            builder.addAction(playAction)
        }
        builder.addAction(nextAction)

        val mediaStyle = MediaStyle().setShowActionsInCompactView(0, 1, 2)

        val openIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, openIntent, PendingIntent.FLAG_IMMUTABLE)

        builder.setContentIntent(pendingIntent)
            .setContentTitle(metadata?.title)
            .setContentText(metadata?.artist)
            .setDeleteIntent(stopPendingIntent)
            .setOnlyAlertOnce(true)
            .setSmallIcon(R.drawable.ic_notification)
            .setStyle(mediaStyle)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        metadata?.artworkUri?.let { uri ->
            scope.launch(IO) {
                val bitmap = context.imageLoader.execute(ImageRequest.Builder(context).data(uri).build()).drawable?.toBitmap()
                builder.setLargeIcon(bitmap)
                scope.launch(Main) {
                    onNotificationChangedCallback.onNotificationChanged(MediaNotification(NOW_PLAYING_NOTIFICATION, builder.build()))
                }
            }
        }

        return MediaNotification(
            NOW_PLAYING_NOTIFICATION,
            builder.build()
        )
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
