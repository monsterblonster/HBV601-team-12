package `is`.hi.hbv601_team_12.data.onlineRepositories

import `is`.hi.hbv601_team_12.data.api.RetrofitClient
import `is`.hi.hbv601_team_12.data.entities.Group
import `is`.hi.hbv601_team_12.data.models.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import java.io.IOException

class OnlineGroupsRepository {

    suspend fun createGroup(group: Group, username: String): Response<Group> {
        return try {
            val response = RetrofitClient.groupsApiService.createGroup(group, username)
            if (!response.isSuccessful) {
                println("createGroup failed: ${response.errorBody()?.string()}")
            }
            response
        } catch (e: IOException) {
            println("Network Error in createGroup: ${e.message}")
            createErrorResponse(e)
        }
    }

    suspend fun getGroup(groupId: Long): Response<Group> {
        return try {
            val response = RetrofitClient.groupsApiService.getGroup(groupId)
            if (!response.isSuccessful) {
                println("getGroup failed: ${response.errorBody()?.string()}")
            }
            response
        } catch (e: IOException) {
            println("Network Error in getGroup: ${e.message}")
            createErrorResponse(e)
        }
    }

    suspend fun deleteGroup(groupId: Long): Response<ResponseBody> {
        return try {
            val response = RetrofitClient.groupsApiService.deleteGroup(groupId)
            if (!response.isSuccessful) {
                println("deleteGroup failed: ${response.errorBody()?.string()}")
            }
            response
        } catch (e: IOException) {
            println("Network Error in deleteGroup: ${e.message}")
            createErrorResponse(e)
        }
    }

    suspend fun editGroup(groupId: Long, updatedGroup: Group): Response<Group> {
        return try {
            val response = RetrofitClient.groupsApiService.editGroup(groupId, updatedGroup)
            if (!response.isSuccessful) {
                println("editGroup failed: ${response.errorBody()?.string()}")
            }
            response
        } catch (e: IOException) {
            println("Network Error in editGroup: ${e.message}")
            createErrorResponse(e)
        }
    }

    suspend fun removeUserFromGroup(groupId: Long, userId: Long, currentUserId: Long): Response<Group> {
        return try {
            val response = RetrofitClient.groupsApiService.removeUserFromGroup(groupId, userId, currentUserId)
            if (!response.isSuccessful) {
                println("removeUserFromGroup failed: ${response.errorBody()?.string()}")
            }
            response
        } catch (e: IOException) {
            println("Network Error in removeUserFromGroup: ${e.message}")
            createErrorResponse(e)
        }
    }

    suspend fun getGroupUsers(groupId: Long): Response<List<Long>> {
        return try {
            val response = RetrofitClient.groupsApiService.getGroupUsers(groupId)
            if (!response.isSuccessful) {
                println("getGroupUsers failed: ${response.errorBody()?.string()}")
            }
            response
        } catch (e: IOException) {
            println("Network Error in getGroupUsers: ${e.message}")
            createErrorResponse(e)
        }
    }

    suspend fun getGroupTags(groupId: Long): Response<List<String>> {
        return try {
            val response = RetrofitClient.groupsApiService.getGroupTags(groupId)
            if (!response.isSuccessful) {
                println("getGroupTags failed: ${response.errorBody()?.string()}")
            }
            response
        } catch (e: IOException) {
            println("Network Error in getGroupTags: ${e.message}")
            createErrorResponse(e)
        }
    }

    suspend fun uploadGroupPicture(groupId: Long, imagePart: MultipartBody.Part): Response<ImageUploadResponse> {
        return try {
            val response = RetrofitClient.groupsApiService.uploadGroupPicture(groupId, imagePart)
            if (!response.isSuccessful) {
                println("uploadGroupPicture failed: ${response.errorBody()?.string()}")
            }
            response
        } catch (e: IOException) {
            println("Network Error in uploadGroupPicture: ${e.message}")
            createErrorResponse(e)
        }
    }

    suspend fun getGroupsForUser(userId: Long): Response<List<Group>> {
        return try {
            val response = RetrofitClient.userApiService.getUserGroups(userId)
            if (!response.isSuccessful) {
                println("getGroupsForUser failed: ${response.errorBody()?.string()}")
            }
            response
        } catch (e: IOException) {
            println("Network Error in getGroupsForUser: ${e.message}")
            createErrorResponse(e)
        }
    }

    private fun <T> createErrorResponse(e: IOException): Response<T> {
        return Response.error(
            500,
            "Network Error: ${e.message}".toResponseBody("text/plain".toMediaTypeOrNull())
        )
    }
}
