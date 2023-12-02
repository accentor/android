package me.vanpetegem.accentor.data.users

data class ApiUser(
    val id: Int,
    val name: String,
    val permission: Permission,
)
