package `is`.hi.hbv601_team_12.data.offlineRepositories

import `is`.hi.hbv601_team_12.data.daos.UserDao
import `is`.hi.hbv601_team_12.data.entities.User
import kotlinx.coroutines.flow.Flow

class OfflineUsersRepository(private val userDao: UserDao) {

    suspend fun cacheUser(user: User) {
        userDao.insert(user)
    }

    fun getAllUsersStream(): Flow<List<User>> = userDao.getAllUsers()

    fun getUserStream(id: Long): Flow<User?> = userDao.getUser(id)

    suspend fun clearCache(user: User) {
        userDao.delete(user)
    }

    suspend fun getUserById(userId: Long): User? {
        return userDao.getUserById(userId)
    }

    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }

    suspend fun getUserByUsername(username: String): User? {
        return userDao.getUserByUsername(username)
    }
}
