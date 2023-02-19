package me.vanpetegem.accentor.data.albums

import android.util.SparseArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.Transformations.switchMap
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Upsert
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Dao
abstract class AlbumDao {
    open fun getAll(): LiveData<List<Album>> = switchMap(getAllDbAlbums()) { albums ->
        switchMap(albumArtistsByAlbumId()) { albumArtists ->
            map(albumLabelsByAlbumId()) { albumLabels ->
                albums.map { a -> Album.fromDb(a, albumLabels.get(a.id, ArrayList()), albumArtists.get(a.id, ArrayList())) }
            }
        }
    }

    open fun getAllByPlayed(): LiveData<List<Album>> = switchMap(getAllDbAlbumsByPlayed()) { albums ->
        switchMap(albumArtistsByAlbumId()) { albumArtists ->
            map(albumLabelsByAlbumId()) { albumLabels ->
                albums.map { a -> Album.fromDb(a, albumLabels.get(a.id, ArrayList()), albumArtists.get(a.id, ArrayList())) }
            }
        }
    }

    open fun getByIds(ids: List<Int>): List<Album> {
        val albums = getDbAlbumsByIds(ids)
        val albumsByIds = SparseArray<DbAlbum>()
        albums.forEach { albumsByIds.put(it.id, it) }
        val albumArtists = getAlbumArtistsByAlbumIdWhereAlbumIds(ids)
        val albumLabels = getAlbumLabelsByAlbumIdWhereAlbumIds(ids)
        return ids.map { Album.fromDb(albumsByIds.get(it), albumLabels.get(it, ArrayList()), albumArtists.get(it, ArrayList())) }
    }

    open fun findByIds(ids: List<Int>): LiveData<List<Album>> = switchMap(findDbAlbumsByIds(ids)) { albums ->
        switchMap(albumArtistsByAlbumIdWhereAlbumIds(ids)) { albumArtists ->
            map(albumLabelsByAlbumIdWhereAlbumIds(ids)) { albumLabels ->
                albums.map { a -> Album.fromDb(a, albumLabels.get(a.id, ArrayList()), albumArtists.get(a.id, ArrayList())) }
            }
        }
    }

    open fun findById(id: Int): LiveData<Album?> = switchMap(findDbAlbumById(id)) { dbAlbum ->
        switchMap(findDbAlbumArtistsById(id)) { albumArtists ->
            map(findDbAlbumLabelsById(id)) { albumLabels ->
                if (dbAlbum != null) {
                    Album.fromDb(
                        dbAlbum,
                        albumLabels.map { AlbumLabel(it.labelId, it.catalogueNumber) },
                        albumArtists.map { AlbumArtist(it.artistId, it.name, it.normalizedName, it.order, it.separator) }
                    )
                } else {
                    null
                }
            }
        }
    }

    open fun findByDay(day: LocalDate): LiveData<List<Album>> =
        switchMap(findDbAlbumsByDay(day.format(DateTimeFormatter.ISO_LOCAL_DATE).substring(4))) { albums ->
            switchMap(albumArtistsByAlbumId()) { albumArtists ->
                map(albumLabelsByAlbumId()) { albumLabels ->
                    albums.map { a -> Album.fromDb(a, albumLabels.get(a.id, ArrayList()), albumArtists.get(a.id, ArrayList())) }
                }
            }
        }

    @Transaction
    open fun getAlbumById(id: Int): Album? {
        val dbAlbum = getDbAlbumById(id)
        dbAlbum ?: return null
        val albumArtists = getDbAlbumArtistsById(id)
        val albumLabels = getDbAlbumLabelsById(id)

        return Album.fromDb(
            dbAlbum,
            albumLabels.map { AlbumLabel(it.labelId, it.catalogueNumber) },
            albumArtists.map { AlbumArtist(it.artistId, it.name, it.normalizedName, it.order, it.separator) }
        )
    }

    @Query("SELECT * FROM albums WHERE id = :id")
    protected abstract fun getDbAlbumById(id: Int): DbAlbum?

    @Query("SELECT * FROM albums WHERE id = :id")
    protected abstract fun findDbAlbumById(id: Int): LiveData<DbAlbum?>

    @Query("SELECT * FROM albums WHERE id IN (:ids)")
    protected abstract fun findDbAlbumsByIds(ids: List<Int>): LiveData<List<DbAlbum>>

    @Query("SELECT * FROM albums WHERE id IN (:ids)")
    protected abstract fun getDbAlbumsByIds(ids: List<Int>): List<DbAlbum>

    @Query(
        """
           SELECT * FROM albums WHERE release LIKE '%' || :day || '%'
             ORDER BY release DESC,
                      normalized_title ASC,
                      edition ASC,
                      edition_description ASC,
                      id ASC
        """
    )
    protected abstract fun findDbAlbumsByDay(day: String): LiveData<List<DbAlbum>>

    @Query("SELECT * FROM album_artists WHERE album_id = :id")
    protected abstract fun getDbAlbumArtistsById(id: Int): List<DbAlbumArtist>

    @Query("SELECT * FROM album_artists WHERE album_id = :id")
    protected abstract fun findDbAlbumArtistsById(id: Int): LiveData<List<DbAlbumArtist>>

    @Query("SELECT * FROM album_labels WHERE album_id = :id")
    protected abstract fun getDbAlbumLabelsById(id: Int): List<DbAlbumLabel>

    @Query("SELECT * FROM album_labels WHERE album_id = :id")
    protected abstract fun findDbAlbumLabelsById(id: Int): LiveData<List<DbAlbumLabel>>

    protected open fun albumLabelsByAlbumId(): LiveData<SparseArray<MutableList<AlbumLabel>>> =
        map(getAllAlbumLabels()) {
            val map = SparseArray<MutableList<AlbumLabel>>()
            for (al in it) {
                val l = map.get(al.albumId, ArrayList())
                l.add(AlbumLabel(al.labelId, al.catalogueNumber))
                map.put(al.albumId, l)
            }
            return@map map
        }

    protected open fun albumLabelsByAlbumIdWhereAlbumIds(ids: List<Int>): LiveData<SparseArray<MutableList<AlbumLabel>>> =
        map(findAllAlbumLabelsWhereAlbumIds(ids)) {
            val map = SparseArray<MutableList<AlbumLabel>>()
            for (al in it) {
                val l = map.get(al.albumId, ArrayList())
                l.add(AlbumLabel(al.labelId, al.catalogueNumber))
                map.put(al.albumId, l)
            }
            return@map map
        }

    protected open fun getAlbumLabelsByAlbumIdWhereAlbumIds(ids: List<Int>): SparseArray<MutableList<AlbumLabel>> {
        val albumLabels = getAllAlbumLabelsWhereAlbumIds(ids)
        val map = SparseArray<MutableList<AlbumLabel>>()
        for (al in albumLabels) {
            val l = map.get(al.albumId, ArrayList())
            l.add(AlbumLabel(al.labelId, al.catalogueNumber))
            map.put(al.albumId, l)
        }
        return map
    }

    protected open fun albumArtistsByAlbumId(): LiveData<SparseArray<MutableList<AlbumArtist>>> =
        map(getAllAlbumArtists()) {
            val map = SparseArray<MutableList<AlbumArtist>>()
            for (aa in it) {
                val l = map.get(aa.albumId, ArrayList())
                l.add(AlbumArtist(aa.artistId, aa.name, aa.normalizedName, aa.order, aa.separator))
                map.put(aa.albumId, l)
            }
            return@map map
        }

    protected open fun albumArtistsByAlbumIdWhereAlbumIds(ids: List<Int>): LiveData<SparseArray<MutableList<AlbumArtist>>> =
        map(findAllAlbumArtistsWhereAlbumIds(ids)) {
            val map = SparseArray<MutableList<AlbumArtist>>()
            for (aa in it) {
                val l = map.get(aa.albumId, ArrayList())
                l.add(AlbumArtist(aa.artistId, aa.name, aa.normalizedName, aa.order, aa.separator))
                map.put(aa.albumId, l)
            }
            return@map map
        }

    protected open fun getAlbumArtistsByAlbumIdWhereAlbumIds(ids: List<Int>): SparseArray<MutableList<AlbumArtist>> {
        val albumArtists = getAllAlbumArtistsWhereAlbumIds(ids)
        val map = SparseArray<MutableList<AlbumArtist>>()
        for (aa in albumArtists) {
            val l = map.get(aa.albumId, ArrayList())
            l.add(AlbumArtist(aa.artistId, aa.name, aa.normalizedName, aa.order, aa.separator))
            map.put(aa.albumId, l)
        }
        return map
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
                    album.fetchedAt
                )
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

    @Query("SELECT * FROM albums ORDER BY normalized_title ASC, release ASC, edition ASC, edition_description ASC, id ASC")
    protected abstract fun getAllDbAlbums(): LiveData<List<DbAlbum>>

    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
           SELECT * FROM albums INNER JOIN (
               SELECT tracks.album_id as album_id, MAX(plays.played_at) AS played_at FROM
                   tracks INNER JOIN plays ON tracks.id = plays.track_id GROUP BY tracks.album_id
           ) p ON p.album_id = albums.id ORDER BY p.played_at DESC
        """
    )
    protected abstract fun getAllDbAlbumsByPlayed(): LiveData<List<DbAlbum>>

    @Query("SELECT * FROM album_artists")
    protected abstract fun getAllAlbumArtists(): LiveData<List<DbAlbumArtist>>

    @Query("SELECT * FROM album_artists WHERE album_id IN (:ids)")
    protected abstract fun findAllAlbumArtistsWhereAlbumIds(ids: List<Int>): LiveData<List<DbAlbumArtist>>

    @Query("SELECT * FROM album_artists WHERE album_id IN (:ids)")
    protected abstract fun getAllAlbumArtistsWhereAlbumIds(ids: List<Int>): List<DbAlbumArtist>

    @Query("SELECT * FROM album_labels")
    protected abstract fun getAllAlbumLabels(): LiveData<List<DbAlbumLabel>>

    @Query("SELECT * FROM album_labels WHERE album_id IN (:ids)")
    protected abstract fun findAllAlbumLabelsWhereAlbumIds(ids: List<Int>): LiveData<List<DbAlbumLabel>>

    @Query("SELECT * FROM album_labels WHERE album_id IN (:ids)")
    protected abstract fun getAllAlbumLabelsWhereAlbumIds(ids: List<Int>): List<DbAlbumLabel>

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
