package me.vanpetegem.accentor.data.plays

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UnreportedPlayDao {
    @Query("SELECT * FROM unreported_plays")
    fun getAllUnreportedPlays(): List<UnreportedPlay>

    @Insert
    fun insert(play: UnreportedPlay)

    @Delete
    fun delete(play: UnreportedPlay)
}
