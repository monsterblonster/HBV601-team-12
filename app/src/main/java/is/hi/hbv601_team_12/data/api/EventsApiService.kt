package `is`.hi.hbv601_team_12.data.api

import `is`.hi.hbv601_team_12.data.entities.Comment
import `is`.hi.hbv601_team_12.data.entities.Event
import `is`.hi.hbv601_team_12.data.entities.User
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface EventsApiService {

    @POST("event/create")
    suspend fun createEvent(
        @Query("userId") userId: Long,
        @Query("groupId") groupId: Long,
        @Body event: Event
    ): Response<Event>

    @GET("event/{id}")
    suspend fun getEvent(@Path("id") eventId: Long): Response<Event>

    @PATCH("event/{id}/edit")
    suspend fun editEvent(
        @Path("id") eventId: Long,
        @Query("userId") userId: Long,
        @Body updatedEvent: Event
    ): Response<Event>

    @DELETE("event/{id}/delete")
    suspend fun deleteEvent(
        @Path("id") eventId: Long,
        @Query("userId") userId: Long
    ): Response<ResponseBody>

    @POST("event/{eventId}/going/{userId}")
    suspend fun addUserToGoing(
        @Path("eventId") eventId: Long,
        @Path("userId") userId: Long
    ): Response<Event>

    @POST("event/{eventId}/maybe/{userId}")
    suspend fun addUserToMaybe(
        @Path("eventId") eventId: Long,
        @Path("userId") userId: Long
    ): Response<Event>

    @POST("event/{eventId}/cantGo/{userId}")
    suspend fun addUserToCantGo(
        @Path("eventId") eventId: Long,
        @Path("userId") userId: Long
    ): Response<Event>

    @GET("event/{id}/goingUsers")
    suspend fun getGoingUsers(@Path("id") eventId: Long): Response<List<User>>

    @GET("event/{id}/maybeUsers")
    suspend fun getMaybeUsers(@Path("id") eventId: Long): Response<List<User>>

    @GET("event/{id}/cantGoUsers")
    suspend fun getCantGoUsers(@Path("id") eventId: Long): Response<List<User>>

    @GET("event/{id}/invitedUsers")
    suspend fun getInvitedUsers(@Path("id") eventId: Long): Response<List<User>>


    @POST("event/{eventId}/comment")
    suspend fun postComment(
        @Path("eventId") eventId: Long,
        @Query("userId") userId: Long,
        @Body comment: Comment
    ): Response<Comment>

    @PATCH("event/{eventId}/comment/{commentId}")
    suspend fun editComment(
        @Path("eventId") eventId: Long,
        @Path("commentId") commentId: Long,
        @Query("userId") userId: Long,
        @Body updatedComment: Comment
    ): Response<Comment>

    @DELETE("event/{eventId}/comment/{commentId}")
    suspend fun deleteComment(
        @Path("eventId") eventId: Long,
        @Path("commentId") commentId: Long,
        @Query("userId") userId: Long
    ): Response<ResponseBody>

    @GET("event/{eventId}/comments")
    suspend fun getEventComments(@Path("eventId") eventId: Long): Response<List<Comment>>

    @GET("event/{eventId}/comment/{commentId}")
    suspend fun getSingleComment(
        @Path("eventId") eventId: Long,
        @Path("commentId") commentId: Long
    ): Response<Comment>

    @GET("event/group/{groupId}")
    suspend fun getEventsByGroupId(@Path("groupId") groupId: Long): Response<List<Event>>

    @GET("user/{id}/events")
    suspend fun getEventsForUser(@Path("id") userId: Long): Response<List<Event>>


    @POST("event/{eventId}/invite/{userId}")
    suspend fun inviteUserToEvent(
        @Path("eventId") eventId: Long,
        @Path("userId") userId: Long
    ): Response<Unit>

}
