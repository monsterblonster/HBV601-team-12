package `is`.hi.hbv601_team_12.data.api

import `is`.hi.hbv601_team_12.data.entities.Group
import `is`.hi.hbv601_team_12.data.models.BasicResponse
import `is`.hi.hbv601_team_12.data.entities.Invitation
import `is`.hi.hbv601_team_12.data.entities.User
import `is`.hi.hbv601_team_12.data.models.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface UserApiService {

    @POST("user/register")
    suspend fun registerUser(@Body user: User): Response<User>

    @POST("user/login")
    suspend fun loginUser(@Body user: User): Response<LoginResponse>

    @GET("user/{id}")
    suspend fun getUserProfile(@Path("id") userId: Long): Response<User>

    @PATCH("user/{id}/edit")
    suspend fun updateUser(@Path("id") userId: Long, @Body updatedUser: User): Response<User>

    @Multipart
    @POST("user/{id}/uploadProfilePicture")
    suspend fun uploadProfilePicture(
        @Path("id") userId: Long,
        @Part picture: MultipartBody.Part
    ): Response<ImageUploadResponse>

    @GET("user/{id}/invites")
    suspend fun getUserInvites(
        @Path("id") userId: Long
    ): Response<List<Invitation>>

    @DELETE("user/{id}/invites/{inviteId}/accept")
    suspend fun acceptInvite(
        @Path("id") userId: Long,
        @Path("inviteId") inviteId: Long
    ): Response<BasicResponse>

    @DELETE("user/{id}/invites/{inviteId}/decline")
    suspend fun declineInvite(
        @Path("id") userId: Long,
        @Path("inviteId") inviteId: Long
    ): Response<BasicResponse>

    @GET("user/{id}/groups")
    suspend fun getUserGroups(
        @Path("id") userId: Long
    ): Response<List<Group>>


}
