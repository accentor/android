package me.vanpetegem.accentor.data.albums

import android.util.SparseArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import java.time.Instant

@Dao
abstract class AlbumDao {
    open fun getAll(): LiveData<List<Album>> =
        getAllDbAlbums().switchMap { albums ->
            albumArtistsByAlbumId().switchMap { albumArtists ->
                albumLabelsByAlbumId().map { albumLabels ->
                    albums.map { a -> Album.fromDb(a, albumLabels.get(a.id, ArrayList()), albumArtists.get(a.id, ArrayList())) }
                }
            }
        }

    protected open fun albumLabelsByAlbumId(): LiveData<SparseArray<MutableList<AlbumLabel>>> =
        getAllAlbumLabels().map {
            val map = SparseArray<MutableList<AlbumLabel>>()
            for (al in it) {
                val l = map.get(al.albumId, ArrayList())
                l.add(AlbumLabel(al.labelId, al.catalogueNumber))
                map.put(al.albumId, l)
            }
            return@map map
        }

    protected open fun albumArtistsByAlbumId(): LiveData<SparseArray<MutableList<AlbumArtist>>> =
        getAllAlbumArtists().map {
            val map = SparseArray<MutableList<AlbumArtist>>()
            for (aa in it) {
                val l = map.get(aa.albumId, ArrayList())
                l.add(AlbumArtist(aa.artistId, aa.name, aa.normalizedName, aa.order, aa.separator))
                map.put(aa.albumId, l)
            }
            return@map map
        }

    @Transaction
    open fun upsertAll(albums: List<Album>) {
        albums.forEach { album: Album ->
            upsert(
                DbAlbum(
                    album.id,
                    album.title,
                    album.normalizedTitle,
                    album.release,
                    album.reviewComment,
                    album.edition,
                    album.editionDescription,
                    album.createdAt,
                    album.updatedAt,
                    album.image,
                    album.image500,
                    album.image250,
                    album.image100,
                    album.imageType,
                    album.fetchedAt,
                ),
            )
            deleteAlbumLabelsById(album.id)
            for (al: AlbumLabel in album.albumLabels) {
                insert(DbAlbumLabel(album.id, al.labelId, al.catalogueNumber))
            }
            deleteAlbumArtistsById(album.id)
            for (al: AlbumArtist in album.albumArtists) {
                insert(DbAlbumArtist(album.id, al.artistId, al.name, al.normalizedName, al.order, al.separator))
            }
        }
    }

    @Query("SELECT * FROM albums ORDER BY `normalized_title` ASC, `release` ASC, `edition` ASC, `edition_description` ASC, `id` ASC")
    protected abstract fun getAllDbAlbums(): LiveData<List<DbAlbum>>

    @Query(
        """
           SELECT album_id FROM (
               SELECT tracks.album_id as album_id, MAX(plays.played_at) AS played_at FROM
                   tracks INNER JOIN plays ON tracks.id = plays.track_id GROUP BY tracks.album_id
           ) p ORDER BY p.played_at DESC
        """,
    )
    abstract fun getIdsByPlayed(): LiveData<List<Int>>

    @Query("SELECT * FROM album_artists")
    protected abstract fun getAllAlbumArtists(): LiveData<List<DbAlbumArtist>>

    @Query("SELECT * FROM album_labels")
    protected abstract fun getAllAlbumLabels(): LiveData<List<DbAlbumLabel>>

    @Upsert
    protected abstract fun upsert(album: DbAlbum)

    @Insert
    protected abstract fun insert(albumArtist: DbAlbumArtist)

    @Insert
    protected abstract fun insert(albumLabel: DbAlbumLabel)

    @Transaction
    open fun deleteFetchedBefore(time: Instant) {
        deleteAlbumsFetchedBefore(time)
        deleteUnusedAlbumLabels()
        deleteUnusedAlbumArtists()
    }

    @Query("DELETE FROM albums WHERE fetched_at < :time")
    protected abstract fun deleteAlbumsFetchedBefore(time: Instant)

    @Query("DELETE FROM album_artists WHERE album_id NOT IN (SELECT id FROM albums)")
    protected abstract fun deleteUnusedAlbumArtists()

    @Query("DELETE FROM album_labels WHERE album_id NOT IN (SELECT id FROM albums)")
    protected abstract fun deleteUnusedAlbumLabels()

    @Query("DELETE FROM albums")
    protected abstract fun deleteAllAlbums()

    @Query("DELETE FROM album_artists WHERE album_id = :id")
    protected abstract fun deleteAlbumArtistsById(id: Int)

    @Query("DELETE FROM album_artists")
    protected abstract fun deleteAllAlbumArtists()

    @Query("DELETE FROM album_labels WHERE album_id = :id")
    protected abstract fun deleteAlbumLabelsById(id: Int)

    @Query("DELETE FROM album_labels")
    protected abstract fun deleteAllAlbumLabels()

    @Transaction
    open fun deleteAll() {
        deleteAllAlbums()
        deleteAllAlbumArtists()
        deleteAllAlbumLabels()
    }
}
