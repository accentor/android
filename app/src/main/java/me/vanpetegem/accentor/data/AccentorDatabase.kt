package me.vanpetegem.accentor.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import me.vanpetegem.accentor.data.albums.AlbumDao
import me.vanpetegem.accentor.data.albums.DbAlbum
import me.vanpetegem.accentor.data.albums.DbAlbumArtist
import me.vanpetegem.accentor.data.albums.DbAlbumLabel
import me.vanpetegem.accentor.data.artists.ArtistDao
import me.vanpetegem.accentor.data.artists.DbArtist
import me.vanpetegem.accentor.data.users.DbUser
import me.vanpetegem.accentor.data.users.UserDao
import me.vanpetegem.accentor.util.RoomTypeConverters

@TypeConverters(RoomTypeConverters::class)
@Database(
    entities = [DbUser::class, DbAlbum::class, DbAlbumArtist::class, DbAlbumLabel::class, DbArtist::class],
    version = 1
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
                        .fallbackToDestructiveMigration()
                        .build()
                INSTANCE = instance
                instance
            }
        }
    }

    abstract fun userDao(): UserDao
    abstract fun albumDao(): AlbumDao
    abstract fun artistDao(): ArtistDao
}