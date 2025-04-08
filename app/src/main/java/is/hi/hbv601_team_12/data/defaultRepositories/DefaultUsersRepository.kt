package `is`.hi.hbv601_team_12.data.defaultRepositories

import `is`.hi.hbv601_team_12.data.entities.User
import `is`.hi.hbv601_team_12.data.models.LoginResponse
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineUsersRepository
import `is`.hi.hbv601_team_12.data.onlineRepositories.OnlineUsersRepository
import `is`.hi.hbv601_team_12.data.repositories.UsersRepository
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

class DefaultUsersRepository(
    private val offlineRepo: OfflineUsersRepository,
    private val onlineRepo: OnlineUsersRepository
) : UsersRepository {

    override suspend fun registerUser(user: User): Response<User> {
        return onlineRepo.registerUser(user)
    }

    override suspend fun loginUser(username: String, password: String): Response<LoginResponse> {
        return onlineRepo.loginUser(username, password)
    }

    override suspend fun getUserById(id: Long): Response<User> {
        return onlineRepo.getUserById(id)
    }

    override suspend fun updateUser(userId: Long, user: User): Response<User> {
        return onlineRepo.updateUser(userId, user)
    }

    override fun getAllUsersStream(): Flow<List<User>> {
        return offlineRepo.getAllUsersStream()
    }

    override fun getUserStream(id: Long): Flow<User?> {
        return offlineRepo.getUserStream(id)
    }

    override suspend fun cacheUser(user: User) {
        offlineRepo.cacheUser(user)
    }

    override suspend fun deleteUser(user: User) {
        offlineRepo.clearCache(user)
    }

    override suspend fun getUserByIdOffline(id: Long): User? {
        return offlineRepo.getUserById(id)
    }

    override suspend fun getUserByEmail(email: String): User? {
        return offlineRepo.getUserByEmail(email)
    }

    override suspend fun getUserByUsername(username: String): User? {
        return offlineRepo.getUserByUsername(username)
    }

    override suspend fun removeGroup(userId: Long, groupId: Long): Response<User> {
        return onlineRepo.removeGroup(userId, groupId)
    }
}
