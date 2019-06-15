package me.vanpetegem.accentor.data.authentication

data class AuthenticationData(
    val id: Int,
    val userId: Int,
    val deviceId: String,
    val secret: String
)
