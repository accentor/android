package me.vanpetegem.accentor.data.model

data class AuthenticationData(
    val id: Int,
    val user_id: Int,
    val device_id: String,
    val secret: String
)
