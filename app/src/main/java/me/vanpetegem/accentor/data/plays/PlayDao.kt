package me.vanpetegem.accentor.data.plays

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class PlayDao {
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

    @Insert
    protected abstract fun insert(play: DbPlay)

    @Query("DELETE FROM plays")
    abstract fun deleteAll()
}
