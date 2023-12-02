package me.vanpetegem.accentor.data.users

import android.util.SparseArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import me.vanpetegem.accentor.api.user.index
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository
import me.vanpetegem.accentor.util.Result
import java.time.Instant
import javax.inject.Inject

class UserRepository
    @Inject
    constructor(
        private val userDao: UserDao,
        private val authenticationRepository: AuthenticationRepository,
    ) {
        val allUsers: LiveData<List<User>> = userDao.getAll()
        val allUsersById: LiveData<SparseArray<User>> =
            allUsers.map {
                val map = SparseArray<User>()
                it.forEach { u -> map.put(u.id, u) }
                map
            }

        val currentUser: LiveData<User?> =
            authenticationRepository.authData.switchMap { authData ->
                authData ?: return@switchMap null
                allUsersById.map { it[authData.userId] }
            }

        suspend fun refresh(handler: suspend (Result<Unit>) -> Unit) {
            val fetchStart = Instant.now()

            for (result in index(authenticationRepository.server.value!!, authenticationRepository.authData.value!!)) {
                when (result) {
                    is Result.Success -> {
                        val fetchTime = Instant.now()
                        userDao.upsertAll(result.data.map { User.fromApi(it, fetchTime) })
                    }
                    is Result.Error -> {
                        handler(Result.Error(result.exception))
                        return
                    }
                }
            }
            userDao.deleteFetchedBefore(fetchStart)
            handler(Result.Success(Unit))
        }

        suspend fun clear() {
            userDao.deleteAll()
        }
    }
