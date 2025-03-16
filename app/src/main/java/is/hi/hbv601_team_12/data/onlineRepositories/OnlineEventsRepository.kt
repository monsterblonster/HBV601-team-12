package `is`.hi.hbv601_team_12.data.onlineRepositories

import `is`.hi.hbv601_team_12.data.api.RetrofitClient
import `is`.hi.hbv601_team_12.data.entities.Comment
import `is`.hi.hbv601_team_12.data.entities.Event
import `is`.hi.hbv601_team_12.data.entities.User
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import java.io.IOException

class OnlineEventsRepository {

    private val eventsApiService = RetrofitClient.eventsApiService

    suspend fun createEvent(userId: Long, groupId: Long, event: Event): Response<Event> {
        return try {
            val response = eventsApiService.createEvent(userId, groupId, event)
            if (!response.isSuccessful) {
                println("createEvent failed: ${response.errorBody()?.string()}")
            }
            response
        } catch (e: IOException) {
            println("Network Error in createEvent: ${e.message}")
            createErrorResponse(e)
        }
    }

    suspend fun getEvent(eventId: Long): Response<Event> {
        return try {
            val response = eventsApiService.getEvent(eventId)
            if (!response.isSuccessful) {
                println("getEvent failed: ${response.errorBody()?.string()}")
            }
            response
        } catch (e: IOException) {
            println("Network Error in getEvent: ${e.message}")
            createErrorResponse(e)
        }
    }

    suspend fun editEvent(eventId: Long, userId: Long, updatedEvent: Event): Response<Event> {
        return try {
            val response = eventsApiService.editEvent(eventId, userId, updatedEvent)
            if (!response.isSuccessful) {
                println("editEvent failed: ${response.errorBody()?.string()}")
            }
            response
        } catch (e: IOException) {
            println("Network Error in editEvent: ${e.message}")
            createErrorResponse(e)
        }
    }

    suspend fun deleteEvent(eventId: Long, userId: Long): Response<ResponseBody> {
        return try {
            val response = eventsApiService.deleteEvent(eventId, userId)
            if (!response.isSuccessful) {
                println("deleteEvent failed: ${response.errorBody()?.string()}")
            }
            response
        } catch (e: IOException) {
            println("Network Error in deleteEvent: ${e.message}")
            createErrorResponse(e)
        }
    }

    suspend fun addUserToGoing(eventId: Long, userId: Long): Response<Event> {
        return try {
            val response = eventsApiService.addUserToGoing(eventId, userId)
            if (!response.isSuccessful) {
                println("addUserToGoing failed: ${response.errorBody()?.string()}")
            }
            response
        } catch (e: IOException) {
            println("Network Error in addUserToGoing: ${e.message}")
            createErrorResponse(e)
        }
    }

    suspend fun addUserToMaybe(eventId: Long, userId: Long): Response<Event> {
        return try {
            val response = eventsApiService.addUserToMaybe(eventId, userId)
            if (!response.isSuccessful) {
                println("addUserToMaybe failed: ${response.errorBody()?.string()}")
            }
            response
        } catch (e: IOException) {
            println("Network Error in addUserToMaybe: ${e.message}")
            createErrorResponse(e)
        }
    }

    suspend fun addUserToCantGo(eventId: Long, userId: Long): Response<Event> {
        return try {
            val response = eventsApiService.addUserToCantGo(eventId, userId)
            if (!response.isSuccessful) {
                println("addUserToCantGo failed: ${response.errorBody()?.string()}")
            }
            response
        } catch (e: IOException) {
            println("Network Error in addUserToCantGo: ${e.message}")
            createErrorResponse(e)
        }
    }

    // CHANGED: now return Response<List<User>> to match server's JSON
    suspend fun getGoingUsers(eventId: Long): Response<List<User>> {
        return try {
            val response = eventsApiService.getGoingUsers(eventId)
            if (!response.isSuccessful) {
                println("getGoingUsers failed: ${response.errorBody()?.string()}")
            }
            response
        } catch (e: IOException) {
            println("Network Error in getGoingUsers: ${e.message}")
            createErrorResponse(e)
        }
    }

    // CHANGED: match server return type
    suspend fun getMaybeUsers(eventId: Long): Response<List<User>> {
        return try {
            val response = eventsApiService.getMaybeUsers(eventId)
            if (!response.isSuccessful) {
                println("getMaybeUsers failed: ${response.errorBody()?.string()}")
            }
            response
        } catch (e: IOException) {
            println("Network Error in getMaybeUsers: ${e.message}")
            createErrorResponse(e)
        }
    }

    // CHANGED: match server return type
    suspend fun getCantGoUsers(eventId: Long): Response<List<User>> {
        return try {
            val response = eventsApiService.getCantGoUsers(eventId)
            if (!response.isSuccessful) {
                println("getCantGoUsers failed: ${response.errorBody()?.string()}")
            }
            response
        } catch (e: IOException) {
            println("Network Error in getCantGoUsers: ${e.message}")
            createErrorResponse(e)
        }
    }

    suspend fun postComment(eventId: Long, userId: Long, comment: String): Response<Comment> {
        return try {
            val response = eventsApiService.postComment(eventId, userId, comment)
            if (!response.isSuccessful) {
                println("postComment failed: ${response.errorBody()?.string()}")
            }
            response
        } catch (e: IOException) {
            println("Network Error in postComment: ${e.message}")
            createErrorResponse(e)
        }
    }

    suspend fun getEventComments(eventId: Long): Response<List<Comment>> {
        return try {
            val response = eventsApiService.getEventComments(eventId)
            if (!response.isSuccessful) {
                println("getEventComments failed: ${response.errorBody()?.string()}")
            }
            response
        } catch (e: IOException) {
            println("Network Error in getEventComments: ${e.message}")
            createErrorResponse(e)
        }
    }

    private fun <T> createErrorResponse(e: IOException): Response<T> {
        return Response.error(
            500,
            "Network Error: ${e.message}".toResponseBody("text/plain".toMediaTypeOrNull())
        )
    }

    suspend fun getEventsByGroupId(groupId: Long): Response<List<Event>> {
        return try {
            val response = eventsApiService.getEventsByGroupId(groupId)
            response
        } catch (e: IOException) {
            // handle network error
        } as Response<List<Event>>
    }

    suspend fun getEventsForUser(userId: Long): Response<List<Event>> {
        return try {
            val response = eventsApiService.getEventsForUser(userId)
            response
        } catch (e: IOException) {
            // handle network error
        } as Response<List<Event>>
    }

}
