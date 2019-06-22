package me.vanpetegem.accentor.data.users

import android.util.SparseArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.Transformations.switchMap
import me.vanpetegem.accentor.api.user.index
import me.vanpetegem.accentor.data.authentication.AuthenticationRepository
import me.vanpetegem.accentor.util.Result
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class UserRepository(private val userDao: UserDao, private val authenticationRepository: AuthenticationRepository) {
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

    fun refresh(handler: (Result<Unit>) -> Unit) {
        doAsync {
            when (val result =
                index(authenticationRepository.server.value!!, authenticationRepository.authData.value!!)) {
                is Result.Success -> {
                    userDao.replaceAll(result.data)

                    uiThread {
                        handler(Result.Success(Unit))
                    }
                }
                is Result.Error -> uiThread { handler(Result.Error(result.exception)) }
            }
        }
    }

    fun clear() {
        doAsync {
            userDao.deleteAll()
        }
    }
}