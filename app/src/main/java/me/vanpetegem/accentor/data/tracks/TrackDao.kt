package me.vanpetegem.accentor.data.tracks

import android.util.SparseArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.Transformations.switchMap
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import java.time.Instant
import me.vanpetegem.accentor.data.albums.Album
import me.vanpetegem.accentor.data.artists.Artist

@Dao
abstract class TrackDao {

    @Transaction
    open fun getByAlbum(album: Album): List<Track> {
        val tracks = getDbTracksByAlbumId(album.id)
        val ids = tracks.map { it.id }
        val trackGenres = getTrackGenresByTrackIdWhereTrackIds(ids)
        val trackArtists = getTrackArtistsByTrackIdWhereTrackIds(ids)
        return tracks.map { t -> Track.fromDb(t, trackArtists.get(t.id, ArrayList()), trackGenres.get(t.id, ArrayList())) }
    }

    open fun getByIds(ids: List<Int>): List<Track> {
        val tracks = getDbTracksByIds(ids)
        val tracksByIds = SparseArray<DbTrack>()
        tracks.forEach { tracksByIds.put(it.id, it) }
        val trackGenres = getTrackGenresByTrackIdWhereTrackIds(ids)
        val trackArtists = getTrackArtistsByTrackIdWhereTrackIds(ids)
        return ids.map { Track.fromDb(tracksByIds.get(it), trackArtists.get(it, ArrayList()), trackGenres.get(it, ArrayList())) }
    }

    open fun findByIds(ids: List<Int>): LiveData<List<Track>> = switchMap(findDbTracksByIds(ids)) { tracks ->
        switchMap(findTrackArtistsByTrackIdWhereTrackIds(ids)) { trackArtists ->
            map(findTrackGenresByTrackIdWhereTrackIds(ids)) { trackGenres ->
                tracks.map { t -> Track.fromDb(t, trackArtists.get(t.id, ArrayList()), trackGenres.get(t.id, ArrayList())) }
            }
        }
    }

    open fun findById(id: Int): LiveData<Track?> = switchMap(findDbTrackById(id)) { dbTrack ->
        switchMap(findDbTrackArtistsById(id)) { trackArtists ->
            map(findDbTrackGenresById(id)) { trackGenres ->
                dbTrack?.let {
                    Track.fromDb(
                        it,
                        trackArtists.map { TrackArtist(it.artistId, it.name, it.normalizedName, it.role, it.order, it.hidden) },
                        trackGenres.map { it.genreId },
                    )
                }
            }
        }
    }

    open fun findByArtist(artist: Artist): LiveData<List<Track>> = switchMap(findDbTracksByArtistId(artist.id)) { tracks ->
        val ids = tracks.map { it.id }
        switchMap(findTrackArtistsByTrackIdWhereTrackIds(ids)) { trackArtists ->
            map(findTrackGenresByTrackIdWhereTrackIds(ids)) { trackGenres ->
                tracks.map { Track.fromDb(it, trackArtists.get(it.id, ArrayList()), trackGenres.get(it.id, ArrayList())) }
            }
        }
    }

    open fun getByArtistId(id: Int): List<Track> {
        val tracks = getDbTracksByArtistId(id)
        val ids = tracks.map { it.id }
        val trackArtists = getTrackArtistsByTrackIdWhereTrackIds(ids)
        val trackGenres = getTrackGenresByTrackIdWhereTrackIds(ids)
        return tracks.map { Track.fromDb(it, trackArtists.get(it.id, ArrayList()), trackGenres.get(it.id, ArrayList())) }
    }

    open fun findByAlbum(album: Album): LiveData<List<Track>> = switchMap(findDbTracksByAlbumId(album.id)) { tracks ->
        val ids = tracks.map { it.id }
        switchMap(findTrackArtistsByTrackIdWhereTrackIds(ids)) { trackArtists ->
            map(findTrackGenresByTrackIdWhereTrackIds(ids)) { trackGenres ->
                tracks.map { Track.fromDb(it, trackArtists.get(it.id, ArrayList()), trackGenres.get(it.id, ArrayList())) }
            }
        }
    }

    @Transaction
    open fun getTrackById(id: Int): Track? {
        val dbTrack = getDbTrackById(id)
        dbTrack ?: return null
        val trackArtists = getDbTrackArtistsById(id)
        val trackGenres = getDbTrackGenresById(id)

        return Track.fromDb(
            dbTrack,
            trackArtists.map { TrackArtist(it.artistId, it.name, it.normalizedName, it.role, it.order, it.hidden) },
            trackGenres.map { it.genreId },
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

    @Query("SELECT * FROM tracks WHERE id IN (:ids)")
    protected abstract fun getDbTracksByIds(ids: List<Int>): List<DbTrack>

    @Query("SELECT * FROM tracks WHERE album_id = :id")
    protected abstract fun findDbTracksByAlbumId(id: Int): LiveData<List<DbTrack>>

    @Query("SELECT * FROM tracks WHERE id IN (SELECT track_id FROM track_artists WHERE artist_id = :id)")
    protected abstract fun findDbTracksByArtistId(id: Int): LiveData<List<DbTrack>>

    @Query("SELECT * FROM tracks WHERE id IN (SELECT track_id FROM track_artists WHERE artist_id = :id)")
    protected abstract fun getDbTracksByArtistId(id: Int): List<DbTrack>

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
                        ta.order,
                        ta.hidden,
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
                    ta.order,
                    ta.hidden,
                )
            )
            map.put(ta.trackId, l)
        }
        return map
    }

    protected open fun findTrackGenresByTrackIdWhereTrackIds(ids: List<Int>): LiveData<SparseArray<MutableList<Int>>> =
        map(findAllTrackGenresWhereTrackIds(ids)) {
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
    open fun upsertAll(tracks: List<Track>) {
        tracks.forEach { track: Track ->
            upsert(
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
                    track.locationId,
                    track.fetchedAt,
                )
            )
            deleteTrackArtistsById(track.id)
            for (ta in track.trackArtists) {
                insert(
                    DbTrackArtist(
                        track.id,
                        ta.artistId,
                        ta.name,
                        ta.normalizedName,
                        ta.role,
                        ta.order,
                        ta.hidden,
                    )
                )
            }
            deleteTrackGenresById(track.id)
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

    @Upsert
    protected abstract fun upsert(track: DbTrack)

    @Insert
    protected abstract fun insert(track: DbTrack)

    @Insert
    protected abstract fun insert(trackArtist: DbTrackArtist)

    @Insert
    protected abstract fun insert(trackGenre: DbTrackGenre)

    @Transaction
    open fun deleteFetchedBefore(time: Instant) {
        deleteTracksFetchedBefore(time)
        deleteUnusedTrackArtists()
        deleteUnusedTrackGenres()
    }

    @Query("DELETE FROM tracks WHERE fetched_at < :time")
    protected abstract fun deleteTracksFetchedBefore(time: Instant)

    @Query("DELETE FROM track_artists WHERE track_id NOT IN (SELECT id FROM tracks)")
    protected abstract fun deleteUnusedTrackArtists()

    @Query("DELETE FROM track_genres WHERE track_id NOT IN (SELECT id FROM tracks)")
    protected abstract fun deleteUnusedTrackGenres()

    @Query("DELETE FROM track_artists WHERE track_id = :id")
    protected abstract fun deleteTrackArtistsById(id: Int)

    @Query("DELETE FROM track_genres WHERE track_id = :id")
    protected abstract fun deleteTrackGenresById(id: Int)

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
