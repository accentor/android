package me.vanpetegem.accentor.media

import android.app.Notification
import android.content.Intent
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.scheduler.Scheduler
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.audioCache
import me.vanpetegem.accentor.audioDatabaseProvider
import me.vanpetegem.accentor.userAgent

const val PLAY_QUEUE_DOWNLOAD_NOTIFICATION: Int = 0xb440
const val PLAY_QUEUE_DOWNLOAD_CHANNEL: String =
    "me.vanpetegem.accentor.media.PLAY_QUEUE_DOWNLOAD_CHANNEL"


class PlayQueueDownloadService : DownloadService(
    PLAY_QUEUE_DOWNLOAD_NOTIFICATION,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    PLAY_QUEUE_DOWNLOAD_CHANNEL,
    R.string.play_queue_download_notification_channel_name,
    R.string.play_queue_download_notification_channel_description
) {

    override fun getDownloadManager() = DownloadManager(
        applicationContext,
        audioDatabaseProvider,
        audioCache,
        DefaultHttpDataSourceFactory(userAgent, 0, 0, false)
    ).apply {
        maxParallelDownloads = 1
    }

    override fun getForegroundNotification(downloads: MutableList<Download>): Notification =
        NotificationCompat.Builder(
            this,
            PLAY_QUEUE_DOWNLOAD_CHANNEL
        )
            .setSmallIcon(R.drawable.ic_download)
            .setContentTitle(this.getString(R.string.downloading_play_queue))
            .setContentText(
                resources.getQuantityString(
                    R.plurals.downloading_n_tracks, downloads.size, downloads.size
                )
            )
            .build()

    override fun getScheduler(): Scheduler? = null

}
