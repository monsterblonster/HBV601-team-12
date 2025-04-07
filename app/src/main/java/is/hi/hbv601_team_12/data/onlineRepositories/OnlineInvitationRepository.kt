package `is`.hi.hbv601_team_12.data.onlineRepositories

import `is`.hi.hbv601_team_12.data.api.RetrofitClient
import `is`.hi.hbv601_team_12.data.entities.Invitation
import `is`.hi.hbv601_team_12.data.models.BasicResponse
import retrofit2.Response
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody

class OnlineInvitationRepository {

    suspend fun createInvitation(groupId: Long, username: String): Response<Invitation> {
        return try {
            val response = RetrofitClient.groupsApiService.inviteUserToGroup(groupId, username)
            if (!response.isSuccessful) {
                println("createInvitation failed: ${response.errorBody()?.string()}")
            }
            response
        } catch (e: IOException) {
            println("Network Error in createInvitation: ${e.message}")
            createErrorResponse(e)
        }
    }

    suspend fun getUserInvites(userId: Long): Response<List<Invitation>> {
        return try {
            val response = RetrofitClient.userApiService.getUserInvites(userId)
            if (!response.isSuccessful) {
                println("getUserInvites failed: ${response.errorBody()?.string()}")
            }
            response
        } catch (e: IOException) {
            println("Network Error in getUserInvites: ${e.message}")
            createErrorResponse(e)
        }
    }

    suspend fun acceptInvite(userId: Long, inviteId: Long): Response<BasicResponse> {
        return try {
            val response = RetrofitClient.userApiService.acceptInvite(userId, inviteId)
            if (!response.isSuccessful) {
                println("acceptInvite failed: ${response.errorBody()?.string()}")
            }
            response
        } catch (e: IOException) {
            println("Network Error in acceptInvite: ${e.message}")
            createErrorResponse(e)
        }
    }

    suspend fun declineInvite(userId: Long, inviteId: Long): Response<BasicResponse> {
        return try {
            val response = RetrofitClient.userApiService.declineInvite(userId, inviteId)
            if (!response.isSuccessful) {
                println("declineInvite failed: ${response.errorBody()?.string()}")
            }
            response
        } catch (e: IOException) {
            println("Network Error in declineInvite: ${e.message}")
            createErrorResponse(e)
        }
    }
}

    private fun <T> createErrorResponse(e: IOException): Response<T> {
        return Response.error(
            500,
            "Network Error: ${e.message}".toResponseBody("text/plain".toMediaTypeOrNull())
        )
    }
