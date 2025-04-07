package `is`.hi.hbv601_team_12.data.repositories

import `is`.hi.hbv601_team_12.data.entities.User
import `is`.hi.hbv601_team_12.data.models.LoginResponse
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface UsersRepository {

    suspend fun registerUser(user: User): Response<User>
    suspend fun loginUser(username: String, password: String): Response<LoginResponse>
    suspend fun getUserById(id: Long): Response<User>
    suspend fun updateUser(userId: Long, user: User): Response<User>

    fun getAllUsersStream(): Flow<List<User>>
    fun getUserStream(id: Long): Flow<User?>
    suspend fun cacheUser(user: User)
    suspend fun deleteUser(user: User)
    suspend fun getUserByIdOffline(id: Long): User?
    suspend fun getUserByEmail(email: String): User?
    suspend fun getUserByUsername(username: String): User?
}
