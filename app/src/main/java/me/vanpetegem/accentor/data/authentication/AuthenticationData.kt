package me.vanpetegem.accentor.data.authentication

data class AuthenticationData(
    val id: Int,
    val userId: Int,
    val deviceId: String,
    val secret: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AuthenticationData

        if (id != other.id) return false
        if (userId != other.userId) return false
        if (deviceId != other.deviceId) return false
        if (secret != other.secret) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + userId
        result = 31 * result + deviceId.hashCode()
        result = 31 * result + secret.hashCode()
        return result
    }
}
