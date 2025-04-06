package `is`.hi.hbv601_team_12.data.onlineRepositories

import `is`.hi.hbv601_team_12.data.api.RetrofitClient
import `is`.hi.hbv601_team_12.data.entities.Notification
import `is`.hi.hbv601_team_12.data.entities.User
import `is`.hi.hbv601_team_12.data.models.BasicResponse
import `is`.hi.hbv601_team_12.data.models.ImageUploadResponse
import `is`.hi.hbv601_team_12.data.models.LoginResponse
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineUsersRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import java.io.IOException

class OnlineUsersRepository(
    private val offlineCache: OfflineUsersRepository
) {

    suspend fun registerUser(user: User): Response<User> {
        return try {
            val response = RetrofitClient.userApiService.registerUser(user)
            if (response.isSuccessful) {
                response.body()?.let {
                    offlineCache.cacheUser(it)
                    println("Registration Success: $it")
                }
            } else {
                println("Registration Failed: ${response.errorBody()?.string()}")
            }
            response
        } catch (e: IOException) {
            println("Network Error: ${e.message}")
            Response.error(
                500,
                "Network Error: ${e.message}".toResponseBody("text/plain".toMediaTypeOrNull())
            )
        }
    }

    suspend fun loginUser(username: String, password: String): Response<LoginResponse> {
        return try {
            val user = User(
                userName = username,
                userPW = password,
                emailAddress = "",
                fullName = "",
                phoneNumber = null,
                address = null,
                profilePicturePath = null
            )
            println("Sending Login Request: Username = ${user.userName}, Password = ${user.userPW}")

            val response = RetrofitClient.userApiService.loginUser(user)
            if (response.isSuccessful) {
                response.body()?.let { loginResponse ->
                    println("Login Success: $loginResponse")
                }
            } else {
                println("Login Failed: ${response.errorBody()?.string()}")
            }
            response
        } catch (e: IOException) {
            println("Network Error: ${e.message}")
            Response.error(
                500,
                "Network Error: ${e.message}".toResponseBody("text/plain".toMediaTypeOrNull())
            )
        }
    }

    suspend fun getUserById(id: Long): Response<User> {
        return try {
            val response = RetrofitClient.userApiService.getUserProfile(id)
            if (response.isSuccessful) {
                response.body()?.let {
                    offlineCache.cacheUser(it)
                }
            }
            response
        } catch (e: IOException) {
            Response.error(
                500,
                "Network Error: ${e.message}".toResponseBody("text/plain".toMediaTypeOrNull())
            )
        }
    }

    suspend fun updateUser(userId: Long, user: User): Response<User> {
        return try {
            val response = RetrofitClient.userApiService.updateUser(userId, user)
            if (response.isSuccessful) {
                response.body()?.let {
                    offlineCache.cacheUser(it)
                }
            }
            response
        } catch (e: IOException) {
            Response.error(
                500,
                "Network Error: ${e.message}".toResponseBody("text/plain".toMediaTypeOrNull())
            )
        }
    }

    suspend fun uploadProfilePicture(userId: Long, imagePart: MultipartBody.Part): Response<ImageUploadResponse> {
        return try {
            val response = RetrofitClient.userApiService.uploadProfilePicture(userId, imagePart)
            if (response.isSuccessful) {
                println("Image Upload Success: ${response.body()}")
            } else {
                println("Image Upload Failed: ${response.errorBody()?.string()}")
            }
            response
        } catch (e: IOException) {
            println("Network Error: ${e.message}")
            Response.error(
                500,
                "Network Error: ${e.message}".toResponseBody("text/plain".toMediaTypeOrNull())
            )
        }
    }

    suspend fun getUserNotifications(userId: Long): Response<List<Notification>> {
        return try {
            val response = RetrofitClient.userApiService.getUserNotifications(userId)
            if (response.isSuccessful) {
                response.body()?.let {
                    println("Notifications Success: $it")
                }
            } else {
                println("Notifications Failed: ${response.errorBody()?.string()}")
            }
            response
            } catch (e: IOException){
                println("Network Error: ${e.message}")
                Response.error(
                    500,
                    "Network Error: ${e.message}".toResponseBody("text/plain".toMediaTypeOrNull())
                )
            }
    }

    suspend fun clearNotifications(userId: Long): Response<BasicResponse> {
        return try {
            val response = RetrofitClient.userApiService.clearNotifications(userId)
            if (response.isSuccessful) {
                response.body()?.let {
                    println("Clear Notifications Success: $it")
                }
            } else {
                println("Clear Notifications Failed: ${response.errorBody()?.string()}")
            }
            response
        } catch (e: IOException)
        {
            println("Network Error: ${e.message}")
            Response.error(
                500,
                "Network Error: ${e.message}".toResponseBody("text/plain".toMediaTypeOrNull())
            )
            }
    }
}
