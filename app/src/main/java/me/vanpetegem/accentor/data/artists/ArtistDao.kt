package me.vanpetegem.accentor.data.artists

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import java.time.Instant

@Dao
abstract class ArtistDao {
    open fun getAll(): LiveData<List<Artist>> =
        getAllDbArtists().map { list ->
            list.map { Artist.fromDb(it) }
        }

    @Transaction
    open fun upsertAll(artists: List<Artist>) {
        artists.forEach { artist ->
            upsert(
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
                    artist.imageType,
                    artist.fetchedAt,
                ),
            )
        }
    }

    @Query("SELECT * FROM artists ORDER BY normalized_name ASC, id ASC")
    protected abstract fun getAllDbArtists(): LiveData<List<DbArtist>>

    @Query(
        """
           SELECT id FROM artists INNER JOIN (
               SELECT track_artists.artist_id as artist_id, MAX(plays.played_at) as played_at FROM
                   track_artists INNER JOIN tracks ON track_artists.track_id = tracks.id INNER JOIN plays ON tracks.id = plays.track_id
                   WHERE track_artists.hidden = 0 GROUP BY track_artists.artist_id
           ) p ON p.artist_id = artists.id ORDER BY p.played_at DESC, normalized_name ASC, id ASC
        """,
    )
    abstract fun getIdsByPlayed(): LiveData<List<Int>>

    @Upsert
    protected abstract fun upsert(artist: DbArtist)

    @Query("DELETE FROM artists WHERE fetched_at < :time")
    abstract fun deleteFetchedBefore(time: Instant)

    @Query("DELETE FROM artists")
    abstract fun deleteAll()
}
