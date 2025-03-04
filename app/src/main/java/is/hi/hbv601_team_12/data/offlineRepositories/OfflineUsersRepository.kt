package `is`.hi.hbv601_team_12.data.offlineRepositories

import `is`.hi.hbv601_team_12.data.daos.UserDao
import `is`.hi.hbv601_team_12.data.entities.User
import `is`.hi.hbv601_team_12.data.repositories.UsersRepository
import kotlinx.coroutines.flow.Flow

class OfflineUsersRepository(private val userDao: UserDao) : UsersRepository {

    override fun getAllUsersStream(): Flow<List<User>> = userDao.getAllUsers()

    override fun getUserStream(id: Int): Flow<User?> = userDao.getUser(id)

    override suspend fun insertUser(user: User) = userDao.insert(user)

    override suspend fun updateUser(user: User) = userDao.update(user)

    override suspend fun deleteUser(user: User) = userDao.delete(user)

    override suspend fun getUserByEmail(email: String): User? = userDao.getUserByEmail(email)

    override suspend fun getUserByUsername(username: String): User? = userDao.getUserByUsername(username)

    override suspend fun loginUser(username: String, password: String): User? = userDao.login(username, password)
}

