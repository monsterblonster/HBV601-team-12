package `is`.hi.hbv601_team_12.data.api

import `is`.hi.hbv601_team_12.data.entities.Group
import `is`.hi.hbv601_team_12.data.entities.Invitation
import `is`.hi.hbv601_team_12.data.models.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*
import okhttp3.MultipartBody

interface GroupsApiService {

    @POST("group/create")
    suspend fun createGroup(@Body group: Group, @Query("username") username: String): Response<Group>

    @GET("group/{id}")
    suspend fun getGroup(@Path("id") groupId: Long): Response<Group>

    @DELETE("group/{id}")
    suspend fun deleteGroup(@Path("id") groupId: Long): Response<ResponseBody>

    @PATCH("group/{id}")
    suspend fun editGroup(@Path("id") groupId: Long, @Body updatedGroup: Group): Response<Group>

    @POST("group/{id}/invite")
    suspend fun inviteUserToGroup(@Path("id") groupId: Long, @Query("username") username: String): Response<Invitation>

    /*@POST("group/{id}/add-tag")
    suspend fun addTagToGroup(
        @Path("id") groupId: Long,
        @Query("tag") tagName: String
    ): Response<TagResponse>

    @POST("group/{id}/remove-tag")
    suspend fun removeTagFromGroup(
        @Path("id") groupId: Long,
        @Query("tag") tagName: String
    ): Response<TagResponse>*/

    @POST("group/{id}/remove-user/{id2}")
    suspend fun removeUserFromGroup(@Path("id") groupId: Long, @Path("id2") userId: Long, @Query("currentUser") currentUser: String): Response<Group>

    @GET("group/{id}/users")
    suspend fun getGroupUsers(@Path("id") groupId: Long): Response<List<Long>>

    @GET("group/{id}/get-tags")
    suspend fun getGroupTags(@Path("id") groupId: Long): Response<List<String>>

    @Multipart
    @POST("group/{id}/uploadGroupPicture")
    suspend fun uploadGroupPicture(@Path("id") groupId: Long, @Part picture: MultipartBody.Part): Response<ImageUploadResponse>
}
