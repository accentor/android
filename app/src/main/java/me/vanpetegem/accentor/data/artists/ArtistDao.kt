package me.vanpetegem.accentor.data.artists

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class ArtistDao {

    open fun getAllArtists(): LiveData<List<Artist>> = map(getAllDbArtists()) { list ->
        list.map { Artist(it.id, it.name, it.reviewComment, it.createdAt, it.updatedAt, it.image, it.imageType) }
    }

    @Transaction
    open fun replaceArtists(artists: List<Artist>) {
        deleteAll()
        artists.forEach { artist ->
            insert(
                DbArtist(
                    artist.id,
                    artist.name,
                    artist.reviewComment,
                    artist.createdAt,
                    artist.updatedAt,
                    artist.image,
                    artist.imageType
                )
            )
        }
    }

    @Query("SELECT * FROM artists ORDER BY name ASC")
    protected abstract fun getAllDbArtists(): LiveData<List<DbArtist>>

    @Insert
    protected abstract fun insert(album: DbArtist)

    @Query("DELETE FROM artists")
    abstract fun deleteAll()

}