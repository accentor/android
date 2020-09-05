package me.vanpetegem.accentor.data.tracks

import android.util.SparseArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.Transformations.switchMap
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import me.vanpetegem.accentor.data.albums.Album

@Dao
abstract class TrackDao {

    @Transaction
    open fun getByAlbum(album: Album): List<Track> {
        val tracks = getDbTracksByAlbumId(album.id);
        val ids = tracks.map { it.id }
        val trackGenres = getTrackGenresByTrackIdWhereTrackIds(ids)
        val trackArtists = getTrackArtistsByTrackIdWhereTrackIds(ids)
        return tracks.map { t ->
            Track(
                t.id,
                t.title,
                t.normalizedTitle,
                t.number,
                t.albumId,
                t.reviewComment,
                t.createdAt,
                t.updatedAt,
                trackGenres.get(t.id, ArrayList()),
                trackArtists.get(t.id, ArrayList()),
                t.codecId,
                t.length,
                t.bitrate,
                t.locationId
            )
        }
    }

    open fun findByIds(ids: List<Int>): LiveData<List<Track>> = switchMap(findDbTracksByIds(ids)) { tracks ->
        switchMap(findTrackArtistsByTrackIdWhereTrackIds(ids)) { trackArtists ->
            map(findTrackGenresByTrackIdWhereTrackIds(ids)) { trackGenres ->
                tracks.map { t ->
                    Track(
                        t.id,
                        t.title,
                        t.normalizedTitle,
                        t.number,
                        t.albumId,
                        t.reviewComment,
                        t.createdAt,
                        t.updatedAt,
                        trackGenres.get(t.id, ArrayList()),
                        trackArtists.get(t.id, ArrayList()),
                        t.codecId,
                        t.length,
                        t.bitrate,
                        t.locationId
                    )
                }
            }
        }
    }

    open fun findById(id: Int): LiveData<Track?> = switchMap(findDbTrackById(id)) { dbTrack ->
        switchMap(findDbTrackArtistsById(id)) { trackArtists ->
            map(findDbTrackGenresById(id)) { trackGenres ->
                if (dbTrack != null) {
                    Track(
                        dbTrack.id,
                        dbTrack.title,
                        dbTrack.normalizedTitle,
                        dbTrack.number,
                        dbTrack.albumId,
                        dbTrack.reviewComment,
                        dbTrack.createdAt,
                        dbTrack.updatedAt,
                        trackGenres.map { it.genreId },
                        trackArtists.map { TrackArtist(it.artistId, it.name, it.normalizedName, it.role, it.order) },
                        dbTrack.codecId,
                        dbTrack.length,
                        dbTrack.bitrate,
                        dbTrack.locationId
                    )
                } else {
                    null
                }
            }
        }
    }

    @Transaction
    open fun getTrackById(id: Int): Track? {
        val dbTrack = getDbTrackById(id)
        dbTrack ?: return null
        val trackArtists = getDbTrackArtistsById(id)
        val trackGenres = getDbTrackGenresById(id)

        return Track(
            dbTrack.id,
            dbTrack.title,
            dbTrack.normalizedTitle,
            dbTrack.number,
            dbTrack.albumId,
            dbTrack.reviewComment,
            dbTrack.createdAt,
            dbTrack.updatedAt,
            trackGenres.map { it.genreId },
            trackArtists.map { TrackArtist(it.artistId, it.name, it.normalizedName, it.role, it.order) },
            dbTrack.codecId,
            dbTrack.length,
            dbTrack.bitrate,
            dbTrack.locationId
        )
    }

    @Query("SELECT * FROM tracks WHERE id = :id")
    protected abstract fun getDbTrackById(id: Int): DbTrack?

    @Query("SELECT * FROM tracks WHERE album_id = :albumId ORDER BY number ASC")
    protected abstract fun getDbTracksByAlbumId(albumId: Int): List<DbTrack>

    @Query("SELECT * FROM tracks WHERE id = :id")
    protected abstract fun findDbTrackById(id: Int): LiveData<DbTrack?>

    @Query("SELECT * FROM tracks WHERE id IN (:ids)")
    protected abstract fun findDbTracksByIds(ids: List<Int>): LiveData<List<DbTrack>>

    @Query("SELECT * FROM track_artists WHERE track_id = :id")
    protected abstract fun getDbTrackArtistsById(id: Int): List<DbTrackArtist>

    @Query("SELECT * FROM track_artists WHERE track_id = :id")
    protected abstract fun findDbTrackArtistsById(id: Int): LiveData<List<DbTrackArtist>>

    @Query("SELECT * FROM track_genres WHERE track_id = :id")
    protected abstract fun getDbTrackGenresById(id: Int): List<DbTrackGenre>

    @Query("SELECT * FROM track_genres WHERE track_id = :id")
    protected abstract fun findDbTrackGenresById(id: Int): LiveData<List<DbTrackGenre>>

    protected open fun findTrackArtistsByTrackIdWhereTrackIds(ids: List<Int>): LiveData<SparseArray<MutableList<TrackArtist>>> =
        map(findAllTrackArtistsWhereTrackIds(ids)) {
            val map = SparseArray<MutableList<TrackArtist>>()
            for (ta in it) {
                val l = map.get(ta.trackId, ArrayList())
                l.add(
                    TrackArtist(
                        ta.artistId,
                        ta.name,
                        ta.normalizedName,
                        ta.role,
                        ta.order
                    )
                )
                map.put(ta.trackId, l)
            }
            return@map map
        }

    protected open fun getTrackArtistsByTrackIdWhereTrackIds(ids: List<Int>): SparseArray<MutableList<TrackArtist>> {
        val map = SparseArray<MutableList<TrackArtist>>()
        for (ta in getAllTrackArtistsWhereTrackIds(ids)) {
            val l = map.get(ta.trackId, ArrayList())
            l.add(
                TrackArtist(
                    ta.artistId,
                    ta.name,
                    ta.normalizedName,
                    ta.role,
                    ta.order
                )
            )
            map.put(ta.trackId, l)
        }
        return map
    }

    protected open fun findTrackGenresByTrackIdWhereTrackIds(ids: List<Int>): LiveData<SparseArray<MutableList<Int>>> = map(findAllTrackGenresWhereTrackIds(ids)) {
        val map = SparseArray<MutableList<Int>>()
        for (tg in it) {
            val l = map.get(tg.trackId, ArrayList())
            l.add(tg.genreId)
            map.put(tg.trackId, l)
        }
        return@map map
    }

    protected open fun getTrackGenresByTrackIdWhereTrackIds(ids: List<Int>): SparseArray<MutableList<Int>> {
        val map = SparseArray<MutableList<Int>>()
        for (tg in getAllTrackGenresWhereTrackIds(ids)) {
            val l = map.get(tg.trackId, ArrayList())
            l.add(tg.genreId)
            map.put(tg.trackId, l)
        }
        return map
    }

    @Transaction
    open fun replaceAll(tracks: List<Track>) {
        deleteAllTracks()
        deleteAllTrackArtists()
        deleteAllTrackGenres()
        tracks.forEach { track: Track ->
            insert(
                DbTrack(
                    track.id,
                    track.title,
                    track.normalizedTitle,
                    track.number,
                    track.albumId,
                    track.reviewComment,
                    track.createdAt,
                    track.updatedAt,
                    track.codecId,
                    track.length,
                    track.bitrate,
                    track.locationId
                )
            )
            for (ta in track.trackArtists) {
                insert(DbTrackArtist(
                    track.id,
                    ta.artistId,
                    ta.name,
                    ta.normalizedName,
                    ta.role,
                    ta.order
                ))
            }
            for (gId in track.genreIds) {
                insert(DbTrackGenre(track.id, gId))
            }
        }
    }

    @Query("SELECT * FROM track_artists WHERE track_id IN (:ids)")
    protected abstract fun findAllTrackArtistsWhereTrackIds(ids: List<Int>): LiveData<List<DbTrackArtist>>

    @Query("SELECT * FROM track_artists WHERE track_id IN (:ids)")
    protected abstract fun getAllTrackArtistsWhereTrackIds(ids: List<Int>): List<DbTrackArtist>

    @Query("SELECT * FROM track_genres WHERE track_id IN (:ids)")
    protected abstract fun findAllTrackGenresWhereTrackIds(ids: List<Int>): LiveData<List<DbTrackGenre>>

    @Query("SELECT * FROM track_genres WHERE track_id IN (:ids)")
    protected abstract fun getAllTrackGenresWhereTrackIds(ids: List<Int>): List<DbTrackGenre>

    @Insert
    protected abstract fun insert(track: DbTrack)

    @Insert
    protected abstract fun insert(trackArtist: DbTrackArtist)

    @Insert
    protected abstract fun insert(trackGenre: DbTrackGenre)

    @Query("DELETE FROM tracks")
    protected abstract fun deleteAllTracks()

    @Query("DELETE FROM track_artists")
    protected abstract fun deleteAllTrackArtists()

    @Query("DELETE FROM track_genres")
    protected abstract fun deleteAllTrackGenres()

    @Transaction
    open fun deleteAll() {
        deleteAllTracks()
        deleteAllTrackArtists()
        deleteAllTrackGenres()
    }
}
