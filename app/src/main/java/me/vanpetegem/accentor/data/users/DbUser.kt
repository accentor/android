package me.vanpetegem.accentor.data.users

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "users")
data class DbUser(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "permission")
    val permission: Permission,
    @ColumnInfo(name = "fetched_at")
    val fetchedAt: Instant,
)
