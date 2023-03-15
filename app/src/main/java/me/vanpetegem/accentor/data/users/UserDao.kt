package me.vanpetegem.accentor.data.users

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import java.time.Instant

@Dao
abstract class UserDao {
    open fun getAll(): LiveData<List<User>> = getAllDbUsers().map { us ->
        us.map { User.fromDb(it) }
    }

    @Transaction
    open fun upsertAll(users: List<User>) {
        users.forEach { upsert(DbUser(it.id, it.name, it.permission, it.fetchedAt)) }
    }

    @Query("SELECT * FROM users ORDER BY name COLLATE NOCASE ASC")
    protected abstract fun getAllDbUsers(): LiveData<List<DbUser>>

    @Upsert
    protected abstract fun upsert(user: DbUser)

    @Query("DELETE FROM users WHERE fetched_at < :time")
    abstract fun deleteFetchedBefore(time: Instant)

    @Query("DELETE FROM users")
    abstract fun deleteAll()
}
