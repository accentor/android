package me.vanpetegem.accentor.data.artists

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class ArtistDao {

    open fun getAll(): LiveData<List<Artist>> = map(getAllDbArtists()) { list ->
        list.map {
            Artist(
                it.id,
                it.name,
                it.normalizedName,
                it.reviewComment,
                it.createdAt,
                it.updatedAt,
                it.image,
                it.image500,
                it.image250,
                it.image100,
                it.imageType
            )
        }
    }

    @Transaction
    open fun replaceAll(artists: List<Artist>) {
        deleteAll()
        artists.forEach { artist ->
            insert(
                DbArtist(
                    artist.id,
                    artist.name,
                    artist.normalizedName,
                    artist.reviewComment,
                    artist.createdAt,
                    artist.updatedAt,
                    artist.image,
                    artist.image500,
                    artist.image250,
                    artist.image100,
                    artist.imageType
                )
            )
        }
    }

    @Query("SELECT * FROM artists ORDER BY normalized_name ASC, id ASC")
    protected abstract fun getAllDbArtists(): LiveData<List<DbArtist>>

    @Insert
    protected abstract fun insert(album: DbArtist)

    @Query("DELETE FROM artists")
    abstract fun deleteAll()
}
