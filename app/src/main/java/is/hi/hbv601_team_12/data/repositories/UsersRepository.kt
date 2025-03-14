package `is`.hi.hbv601_team_12.data.repositories

import `is`.hi.hbv601_team_12.data.entities.User
import kotlinx.coroutines.flow.Flow

interface UsersRepository {
    fun getAllUsersStream(): Flow<List<User>>
    fun getUserStream(id: Int): Flow<User?>
    suspend fun insertUser(user: User)
    suspend fun updateUser(user: User)
    suspend fun deleteUser(user: User)
    suspend fun getUserByEmail(email: String): User?
    suspend fun getUserByUsername(username: String): User?
    suspend fun loginUser(username: String, password: String): User?
    suspend fun getUserById(id: Int): User?
}

