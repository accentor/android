package me.vanpetegem.accentor.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import me.vanpetegem.accentor.data.albums.AlbumDao
import me.vanpetegem.accentor.data.albums.DbAlbum
import me.vanpetegem.accentor.data.albums.DbAlbumArtist
import me.vanpetegem.accentor.data.albums.DbAlbumLabel
import me.vanpetegem.accentor.data.artists.ArtistDao
import me.vanpetegem.accentor.data.artists.DbArtist
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
        DbTrack::class,
        DbTrackArtist::class,
        DbTrackGenre::class
    ],
    version = 6
)
abstract class AccentorDatabase : RoomDatabase() {

    companion object {
        @Volatile
        private var INSTANCE: AccentorDatabase? = null

        fun getDatabase(context: Context): AccentorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance =
                    Room.databaseBuilder(
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
                        .build()
                INSTANCE = instance
                instance
            }
        }
    }

    abstract fun userDao(): UserDao
    abstract fun albumDao(): AlbumDao
    abstract fun artistDao(): ArtistDao
    abstract fun trackDao(): TrackDao
}
