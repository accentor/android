package me.vanpetegem.accentor.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Instant
import me.vanpetegem.accentor.data.albums.AlbumDao
import me.vanpetegem.accentor.data.albums.DbAlbum
import me.vanpetegem.accentor.data.albums.DbAlbumArtist
import me.vanpetegem.accentor.data.albums.DbAlbumLabel
import me.vanpetegem.accentor.data.artists.ArtistDao
import me.vanpetegem.accentor.data.artists.DbArtist
import me.vanpetegem.accentor.data.codecconversions.CodecConversionDao
import me.vanpetegem.accentor.data.codecconversions.DbCodecConversion
import me.vanpetegem.accentor.data.plays.DbPlay
import me.vanpetegem.accentor.data.plays.PlayDao
import me.vanpetegem.accentor.data.plays.UnreportedPlay
import me.vanpetegem.accentor.data.plays.UnreportedPlayDao
import me.vanpetegem.accentor.data.tracks.DbTrack
import me.vanpetegem.accentor.data.tracks.DbTrackArtist
import me.vanpetegem.accentor.data.tracks.DbTrackGenre
import me.vanpetegem.accentor.data.tracks.TrackDao
import me.vanpetegem.accentor.data.users.DbUser
import me.vanpetegem.accentor.data.users.UserDao
import me.vanpetegem.accentor.util.RoomTypeConverters

@TypeConverters(RoomTypeConverters::class)
@Database(
    entities = [
        DbUser::class,
        DbAlbum::class,
        DbAlbumArtist::class,
        DbAlbumLabel::class,
        DbArtist::class,
        DbCodecConversion::class,
        DbPlay::class,
        DbTrack::class,
        DbTrackArtist::class,
        DbTrackGenre::class,
        UnreportedPlay::class,
    ],
    version = 10
)
abstract class AccentorDatabase : RoomDatabase() {
    abstract fun albumDao(): AlbumDao
    abstract fun artistDao(): ArtistDao
    abstract fun codecConversionDao(): CodecConversionDao
    abstract fun playDao(): PlayDao
    abstract fun trackDao(): TrackDao
    abstract fun userDao(): UserDao
    abstract fun unreportedPlayDao(): UnreportedPlayDao
}

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
    @Provides
    fun provideAccentorDatabase(@ApplicationContext context: Context): AccentorDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AccentorDatabase::class.java,
            "accentor_database"
        )
            .addMigrations(object : Migration(2, 3) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.beginTransaction()
                    try {
                        database.execSQL("ALTER TABLE `album_artists` RENAME TO `album_artists_old`")
                        database.execSQL(
                            """
                            CREATE TABLE `album_artists` (
                                `album_id` INTEGER NOT NULL,
                                `artist_id` INTEGER NOT NULL,
                                `name` TEXT NOT NULL,
                                `order` INTEGER NOT NULL, `
                                separator` TEXT,
                                PRIMARY KEY(`album_id`, `artist_id`, `name`))
                            """
                        )
                        database.execSQL(
                            """
                            INSERT INTO `album_artists` (`album_id`, `artist_id`, `name`, `order`, `separator`)
                                SELECT `album_id`, `artist_id`, `name`, `order`, `join` AS `separator` FROM `album_artists_old`
                            """
                        )
                        database.execSQL("DROP TABLE `album_artists_old`")
                        database.setTransactionSuccessful()
                    } finally {
                        database.endTransaction()
                    }
                }
            })
            .addMigrations(object : Migration(3, 4) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.beginTransaction()
                    try {
                        database.execSQL("ALTER TABLE `albums` ADD COLUMN `image_500` TEXT")
                        database.execSQL("ALTER TABLE `albums` ADD COLUMN `image_250` TEXT")
                        database.execSQL("ALTER TABLE `albums` ADD COLUMN `image_100` TEXT")
                        database.execSQL("ALTER TABLE `artists` ADD COLUMN `image_500` TEXT")
                        database.execSQL("ALTER TABLE `artists` ADD COLUMN `image_250` TEXT")
                        database.execSQL("ALTER TABLE `artists` ADD COLUMN `image_100` TEXT")
                        database.setTransactionSuccessful()
                    } finally {
                        database.endTransaction()
                    }
                }
            })
            .addMigrations(object : Migration(4, 5) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.beginTransaction()
                    try {
                        database.execSQL("ALTER TABLE `album_labels` RENAME TO `album_labels_old`")
                        database.execSQL(
                            """
                            CREATE TABLE `album_labels` (
                                `album_id` INTEGER NOT NULL,
                                `label_id` INTEGER NOT NULL,
                                `catalogue_number` TEXT,
                                PRIMARY KEY(`album_id`, `label_id`)
                            )
                            """
                        )
                        database.execSQL(
                            """
                            INSERT INTO `album_labels` (`album_id`, `label_id`, `catalogue_number`)
                                SELECT `album_id`, `label_id`, `catalogue_number` FROM `album_labels_old`
                            """
                        )
                        database.execSQL("DROP TABLE `album_labels_old`")
                        database.setTransactionSuccessful()
                    } finally {
                        database.endTransaction()
                    }
                }
            })
            .addMigrations(object : Migration(5, 6) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.beginTransaction()
                    try {
                        // The UPDATE statements aren't correct;
                        // they don't strip diacritics. However,
                        // the correct data will be loaded from the
                        // server at some point, so it's not that
                        // bad.
                        database.execSQL("ALTER TABLE `albums` ADD COLUMN `normalized_title` TEXT NOT NULL DEFAULT ''")
                        database.execSQL("UPDATE `albums` SET `normalized_title` = LOWER(`title`)")
                        database.execSQL("ALTER TABLE `artists` ADD COLUMN `normalized_name` TEXT NOT NULL DEFAULT ''")
                        database.execSQL("UPDATE `artists` SET `normalized_name` = LOWER(`name`)")
                        database.execSQL("ALTER TABLE `tracks` ADD COLUMN `normalized_title` TEXT NOT NULL DEFAULT ''")
                        database.execSQL("UPDATE `tracks` SET `normalized_title` = LOWER(`title`)")
                        database.execSQL("ALTER TABLE `album_artists` ADD COLUMN `normalized_name` TEXT NOT NULL DEFAULT ''")
                        database.execSQL("UPDATE `album_artists` SET `normalized_name` = LOWER(`name`)")
                        database.execSQL("ALTER TABLE `track_artists` ADD COLUMN `normalized_name` TEXT NOT NULL DEFAULT ''")
                        database.execSQL("UPDATE `track_artists` SET `normalized_name` = LOWER(`name`)")
                        database.setTransactionSuccessful()
                    } finally {
                        database.endTransaction()
                    }
                }
            })
            .addMigrations(object : Migration(6, 7) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.beginTransaction()
                    try {
                        database.execSQL(
                            """
                            CREATE TABLE IF NOT EXISTS `codec_conversions` (
                                `id` INTEGER NOT NULL,
                                `name` TEXT NOT NULL,
                                `ffmpeg_params` TEXT NOT NULL,
                                `resulting_codec_id` INTEGER NOT NULL,
                                PRIMARY KEY(`id`)
                            )
                            """
                        )
                        database.setTransactionSuccessful()
                    } finally {
                        database.endTransaction()
                    }
                }
            })
            .addMigrations(object : Migration(7, 8) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.beginTransaction()
                    try {
                        database.execSQL(
                            """
                            CREATE TABLE IF NOT EXISTS `unreported_plays` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `track_id` INTEGER NOT NULL,
                                `played_at` TEXT NOT NULL
                            )
                            """
                        )
                        database.setTransactionSuccessful()
                    } finally {
                        database.endTransaction()
                    }
                }
            })
            .addMigrations(object : Migration(8, 9) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.beginTransaction()
                    try {
                        database.execSQL(
                            """
                            CREATE TABLE IF NOT EXISTS `plays` (
                                `id` INTEGER NOT NULL,
                                `played_at` TEXT NOT NULL,
                                `track_id` INTEGER NOT NULL,
                                `user_id` INTEGER NOT NULL,
                                PRIMARY KEY(`id`)
                            )
                            """
                        )
                        database.setTransactionSuccessful()
                    } finally {
                        database.endTransaction()
                    }
                }
            })
            .addMigrations(object : Migration(9, 10) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.beginTransaction()
                    try {
                        val now = Instant.now()
                        database.execSQL("ALTER TABLE `tracks` ADD COLUMN `fetched_at` TEXT NOT NULL DEFAULT '$now'")
                        database.execSQL("ALTER TABLE `users` ADD COLUMN `fetched_at` TEXT NOT NULL DEFAULT '$now'")
                        database.execSQL("ALTER TABLE `codec_conversions` ADD COLUMN `fetched_at` TEXT NOT NULL DEFAULT '$now'")
                        database.execSQL("ALTER TABLE `artists` ADD COLUMN `fetched_at` TEXT NOT NULL DEFAULT '$now'")
                        database.setTransactionSuccessful()
                    } finally {
                        database.endTransaction()
                    }
                }
            })
            .build()
    }

    @Provides
    fun provideAlbumDao(database: AccentorDatabase): AlbumDao = database.albumDao()
    @Provides
    fun provideArtistDao(database: AccentorDatabase): ArtistDao = database.artistDao()
    @Provides
    fun provideCodecConversionDao(database: AccentorDatabase): CodecConversionDao = database.codecConversionDao()
    @Provides
    fun providePlayDao(database: AccentorDatabase): PlayDao = database.playDao()
    @Provides
    fun provideTrackDao(database: AccentorDatabase): TrackDao = database.trackDao()
    @Provides
    fun provideUserDao(database: AccentorDatabase): UserDao = database.userDao()
    @Provides
    fun provideUnreportedPlayDao(database: AccentorDatabase): UnreportedPlayDao = database.unreportedPlayDao()
}
