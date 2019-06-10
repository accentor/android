package me.vanpetegem.accentor.data.model

/**
 * Data class that captures user information for logged in users retrieved from AuthenticationRepository
 */
data class AuthenticationData(
    val id: Int,
    val user_id: Int,
    val device_id: String,
    val secret: String
)
