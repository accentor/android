package me.vanpetegem.accentor.data.albums

import android.util.SparseArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.Transformations.switchMap
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class AlbumDao {

    open fun getAll(): LiveData<List<Album>> = switchMap(getAllDbAlbums()) { albums ->
        switchMap(albumArtistsByAlbumId()) { albumArtists ->
            map(albumLabelsByAlbumId()) { albumLabels ->
                albums.map { a ->
                    Album(
                        a.id,
                        a.title,
                        a.release,
                        a.reviewComment,
                        a.edition,
                        a.editionDescription,
                        a.createdAt,
                        a.updatedAt,
                        a.image,
                        a.imageType,
                        albumLabels.get(a.id, ArrayList()),
                        albumArtists.get(a.id, ArrayList())
                    )
                }
            }
        }

    }

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

    protected open fun albumArtistsByAlbumId(): LiveData<SparseArray<MutableList<AlbumArtist>>> =
        map(getAllAlbumArtists()) {
        val map = SparseArray<MutableList<AlbumArtist>>()
        for (aa in it) {
            val l = map.get(aa.albumId, ArrayList())
            l.add(AlbumArtist(aa.artistId, aa.name, aa.order, aa.join))
            map.put(aa.albumId, l)
        }
        return@map map
    }

    @Transaction
    open fun replaceAll(albums: List<Album>) {
        deleteAllAlbums()
        deleteAllAlbumArtists()
        deleteAllAlbumLabels()
        albums.forEach { album: Album ->
            insert(
                DbAlbum(
                    album.id,
                    album.title,
                    album.release,
                    album.reviewComment,
                    album.edition,
                    album.editionDescription,
                    album.createdAt,
                    album.updatedAt,
                    album.image,
                    album.imageType
                )
            )
            for (al: AlbumLabel in album.albumLabels) {
                insert(DbAlbumLabel(album.id, al.labelId, al.catalogueNumber))
            }
            for (al: AlbumArtist in album.albumArtists) {
                insert(DbAlbumArtist(album.id, al.artistId, al.name, al.order, al.join))
            }
        }
    }


    @Query("SELECT * FROM albums ORDER BY title ASC")
    protected abstract fun getAllDbAlbums(): LiveData<List<DbAlbum>>

    @Query("SELECT * FROM album_artists")
    protected abstract fun getAllAlbumArtists(): LiveData<List<DbAlbumArtist>>

    @Query("SELECT * FROM album_labels")
    protected abstract fun getAllAlbumLabels(): LiveData<List<DbAlbumLabel>>

    @Insert
    protected abstract fun insert(album: DbAlbum)

    @Insert
    protected abstract fun insert(albumArtist: DbAlbumArtist)

    @Insert
    protected abstract fun insert(albumLabel: DbAlbumLabel)

    @Query("DELETE FROM albums")
    protected abstract fun deleteAllAlbums()

    @Query("DELETE FROM album_artists")
    protected abstract fun deleteAllAlbumArtists()

    @Query("DELETE FROM album_labels")
    protected abstract fun deleteAllAlbumLabels()

    @Transaction
    open fun deleteAll() {
        deleteAllAlbums()
        deleteAllAlbumArtists()
        deleteAllAlbumLabels()
    }
}