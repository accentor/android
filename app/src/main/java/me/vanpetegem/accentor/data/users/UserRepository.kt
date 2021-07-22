package me.vanpetegem.accentor.data.users

import android.util.SparseArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.Transformations.switchMap
import javax.inject.Inject
import me.vanpetegem.accentor.api.user.index
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository
import me.vanpetegem.accentor.util.Result

class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val authenticationRepository: AuthenticationRepository
) {
    val allUsers: LiveData<List<User>> = userDao.getAll()
    val allUsersById: LiveData<SparseArray<User>> = map(allUsers) {
        val map = SparseArray<User>()
        it.forEach { u -> map.put(u.id, u) }
        map
    }

    val currentUser: LiveData<User?> = switchMap(authenticationRepository.authData) { authData ->
        authData ?: return@switchMap null
        map(allUsersById) { it[authData.userId] }
    }

    suspend fun refresh(handler: suspend (Result<Unit>) -> Unit) {
        when (val result = index(authenticationRepository.server.value!!, authenticationRepository.authData.value!!)) {
            is Result.Success -> {
                userDao.replaceAll(result.data)
                handler(Result.Success(Unit))
            }
            is Result.Error -> handler(Result.Error(result.exception))
        }
    }

    suspend fun clear() {
        userDao.deleteAll()
    }
}
