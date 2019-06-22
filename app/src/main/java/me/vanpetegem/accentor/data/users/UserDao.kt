package me.vanpetegem.accentor.data.users

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class UserDao {

    open fun getAll(): LiveData<List<User>> = map(getAllDbUsers()) { us ->
        us.map { User(it.id, it.name, it.permission) }
    }

    @Transaction
    open fun replaceAll(users: List<User>) {
        deleteAll()
        users.forEach { insert(DbUser(it.id, it.name, it.permission)) }
    }

    @Query("SELECT * FROM users ORDER BY id ASC")
    protected abstract fun getAllDbUsers(): LiveData<List<DbUser>>

    @Insert
    protected abstract fun insert(user: DbUser)

    @Query("DELETE FROM users")
    abstract fun deleteAll()
}