package me.vanpetegem.accentor.data.tracks

import android.util.SparseArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.Transformations.switchMap
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class TrackDao {

    open fun getAll(): LiveData<List<Track>> = switchMap(getAllDbTracks()) { tracks ->
        switchMap(trackArtistsByArtistId()) { trackArtists ->
            map(trackGenresByArtistId()) { trackLabels ->
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
                        trackLabels.get(t.id, ArrayList()),
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

    @Query("SELECT * FROM track_artists WHERE track_id = :id")
    protected abstract fun getDbTrackArtistsById(id: Int): List<DbTrackArtist>

    @Query("SELECT * FROM track_genres WHERE track_id = :id")
    protected abstract fun getDbTrackGenresById(id: Int): List<DbTrackGenre>

    protected open fun trackArtistsByArtistId(): LiveData<SparseArray<MutableList<TrackArtist>>> =
        map(getAllTrackArtists()) {
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

    protected open fun trackGenresByArtistId(): LiveData<SparseArray<MutableList<Int>>> = map(getAllTrackGenres()) {
        val map = SparseArray<MutableList<Int>>()
        for (tg in it) {
            val l = map.get(tg.trackId, ArrayList())
            l.add(tg.genreId)
            map.put(tg.trackId, l)
        }
        return@map map
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

    @Query("SELECT * FROM tracks ORDER BY id ASC")
    protected abstract fun getAllDbTracks(): LiveData<List<DbTrack>>

    @Query("SELECT * FROM track_artists")
    protected abstract fun getAllTrackArtists(): LiveData<List<DbTrackArtist>>

    @Query("SELECT * FROM track_genres")
    protected abstract fun getAllTrackGenres(): LiveData<List<DbTrackGenre>>

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
