package me.vanpetegem.accentor.data.plays

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class PlayDao {
    open fun getAll(): LiveData<List<Play>> = map(getAllDbPlays()) { list ->
        list.map {
            Play(
                it.id,
                it.playedAt,
                it.trackId,
                it.userId,
            )
        }
    }

    @Transaction
    open fun replaceAll(plays: List<Play>) {
        deleteAll()
        plays.forEach { play -> insert(play) }
    }

    open fun insert(play: Play) {
        insert(
            DbPlay(
                play.id,
                play.playedAt,
                play.trackId,
                play.userId,
            )
        )
    }

    @Query("SELECT * FROM plays ORDER BY id ASC")
    protected abstract fun getAllDbPlays(): LiveData<List<DbPlay>>

    @Insert
    protected abstract fun insert(play: DbPlay)

    @Query("DELETE FROM plays")
    abstract fun deleteAll()
}
