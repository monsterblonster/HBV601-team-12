package `is`.hi.hbv601_team_12.data.offlineRepositories

import `is`.hi.hbv601_team_12.data.daos.UserDao
import `is`.hi.hbv601_team_12.data.entities.User
import `is`.hi.hbv601_team_12.data.repositories.UsersRepository
import kotlinx.coroutines.flow.Flow

class OfflineUsersRepository(private val userDao: UserDao) : UsersRepository {
    override fun getAllUsersStream(): Flow<List<User>> {
        return userDao.getAllUsers()
    }

    override fun getUserStream(id: Int): Flow<User> {
        return userDao.getUser(id)
    }

    override suspend fun deleteUser(user: User) {
        return userDao.delete(user)
    }

    override suspend fun insertUser(user: User) {
        return userDao.insert(user)
    }

    override suspend fun updateUser(user: User) {
        return userDao.update(user)
    }
}