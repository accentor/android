package me.vanpetegem.accentor.data.users

import java.time.Instant

data class User(
    val id: Int,
    val name: String,
    val permission: Permission,
    val fetchedAt: Instant,
) {
    companion object {
        fun fromDb(u: DbUser) =
            User(
                u.id,
                u.name,
                u.permission,
                u.fetchedAt,
            )

        fun fromApi(
            u: ApiUser,
            fetchTime: Instant,
        ) = User(
            u.id,
            u.name,
            u.permission,
            fetchTime,
        )
    }
}
