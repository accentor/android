package me.vanpetegem.accentor.data

import me.vanpetegem.accentor.api.create
import me.vanpetegem.accentor.api.destroy
import me.vanpetegem.accentor.data.model.AuthenticationData
import me.vanpetegem.accentor.util.Result

class NetworkAuthenticationDataSource {

    fun login(server: String, username: String, password: String): Result<AuthenticationData> {
        return create(server, username, password)
    }

    fun logout(server: String, authenticationData: AuthenticationData): Result<Unit> {
        return destroy(server, authenticationData, authenticationData.id)
    }
}

