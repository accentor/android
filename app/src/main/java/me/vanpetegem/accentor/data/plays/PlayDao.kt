package me.vanpetegem.accentor.data.plays

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import java.time.Instant

@Dao
abstract class PlayDao {
    @Transaction
    open fun upsertAll(plays: List<Play>) {
        plays.forEach { play -> upsert(play.toDb()) }
    }

    open fun insert(play: Play) {
        insert(play.toDb())
    }

    @Insert
    protected abstract fun insert(play: DbPlay)

    @Upsert
    protected abstract fun upsert(play: DbPlay)

    @Query("DELETE FROM plays WHERE fetched_at < :time")
    abstract fun deleteFetchedBefore(time: Instant)

    @Query("DELETE FROM plays")
    abstract fun deleteAll()
}
