package me.vanpetegem.accentor.data.tracks

import androidx.lifecycle.LiveData
import dagger.Reusable
import me.vanpetegem.accentor.api.track.index
import me.vanpetegem.accentor.data.albums.Album
import me.vanpetegem.accentor.data.artists.Artist
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository
import me.vanpetegem.accentor.util.Result
import java.time.Instant
import javax.inject.Inject

@Reusable
class TrackRepository
    @Inject
    constructor(
        private val trackDao: TrackDao,
        private val authenticationRepository: AuthenticationRepository,
    ) {
        fun findById(id: Int): LiveData<Track?> = trackDao.findById(id)

        fun getById(id: Int): Track? = trackDao.getTrackById(id)

        fun findByIds(ids: List<Int>): LiveData<List<Track>> = trackDao.findByIds(ids)

        fun getByIds(ids: List<Int>): List<Track> = trackDao.getByIds(ids)

        fun findByArtist(artist: Artist): LiveData<List<Track>> = trackDao.findByArtist(artist)

        fun getByArtistId(id: Int): List<Track> = trackDao.getByArtistId(id)

        fun findByAlbum(album: Album): LiveData<List<Track>> = trackDao.findByAlbum(album)

        fun getByAlbum(album: Album): List<Track> = trackDao.getByAlbum(album)

        suspend fun refresh(handler: suspend (Result<Unit>) -> Unit) {
            val fetchStart = Instant.now()

            var toUpsert = ArrayList<Track>()
            var count = 0
            for (result in index(authenticationRepository.server.value!!, authenticationRepository.authData.value!!)) {
                when (result) {
                    is Result.Success -> {
                        val fetchTime = Instant.now()
                        toUpsert.addAll(result.data.map { Track.fromApi(it, fetchTime) })
                        count += 1
                        if (count >= 10) {
                            trackDao.upsertAll(toUpsert)
                            toUpsert.clear()
                            count = 0
                        }
                    }
                    is Result.Error -> {
                        handler(Result.Error(result.exception))
                        return
                    }
                }
            }
            trackDao.upsertAll(toUpsert)
            trackDao.deleteFetchedBefore(fetchStart)
            handler(Result.Success(Unit))
        }

        suspend fun clear() {
            trackDao.deleteAll()
        }
    }
