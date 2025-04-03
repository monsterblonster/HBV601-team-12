package `is`.hi.hbv601_team_12.data.defaultRepositories

import `is`.hi.hbv601_team_12.data.entities.Comment
import `is`.hi.hbv601_team_12.data.entities.Event
import `is`.hi.hbv601_team_12.data.models.BasicResponse
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineEventsRepository
import `is`.hi.hbv601_team_12.data.onlineRepositories.OnlineEventsRepository
import `is`.hi.hbv601_team_12.data.repositories.EventsRepository
import kotlinx.coroutines.flow.Flow
import okhttp3.ResponseBody
import retrofit2.Response

class DefaultEventsRepository(  // aldrei notad!
    private val offlineRepo: OfflineEventsRepository,
    private val onlineRepo: OnlineEventsRepository
) : EventsRepository {

    override fun getAllEventsStream(): Flow<List<Event>> {
        return offlineRepo.getAllEventsStream()
    }

    override fun getEventStream(id: Long): Flow<Event> {
        return offlineRepo.getEventStream(id)
    }

    override suspend fun insertEvent(event: Event) {
        offlineRepo.insertEvent(event)
    }

    override suspend fun updateEvent(event: Event) {
        offlineRepo.updateEvent(event)
    }

    override suspend fun deleteEvent(event: Event) {
        offlineRepo.deleteEvent(event)
    }

    suspend fun createEvent(userId: Long, groupId: Long, event: Event): Response<Event> {
        val response = onlineRepo.createEvent(userId, groupId, event)
        if (response.isSuccessful) {
            response.body()?.let { createdEvent ->
                offlineRepo.insertEvent(createdEvent)
            }
        }
        return response
    }

    suspend fun fetchEventById(eventId: Long): Response<Event> {
        val response = onlineRepo.getEvent(eventId)
        if (response.isSuccessful) {
            response.body()?.let { event ->
                offlineRepo.insertEvent(event)
            }
        }
        return response
    }

    suspend fun getEventById(eventId: Long): Event? {
        val localEvent = offlineRepo.getEventById(eventId)
        return localEvent ?: fetchEventById(eventId).body()
    }

    suspend fun editEvent(eventId: Long, userId: Long, updatedEvent: Event): Response<Event> {
        val response = onlineRepo.editEvent(eventId, userId, updatedEvent)
        if (response.isSuccessful) {
            response.body()?.let { event ->
                offlineRepo.updateEvent(event)
            }
        }
        return response
    }

    suspend fun deleteEventOnline(eventId: Long, userId: Long): Response<ResponseBody> {
        val response = onlineRepo.deleteEvent(eventId, userId)
        if (response.isSuccessful) {
            offlineRepo.getEventById(eventId)?.let {
                offlineRepo.deleteEvent(it)
            }
        }
        return response
    }

    fun getUserEvents(userId: Long): Flow<List<Event>> {
        return offlineRepo.getEventsForUser(userId)
    }

    suspend fun fetchGroupEvents(groupId: Long): Flow<List<Event>> {
        val response = onlineRepo.getEvent(groupId)
        if (response.isSuccessful) {
            response.body()?.let { event ->
                offlineRepo.insertEvent(event)
            }
        }
        return offlineRepo.getEventsForGroupStream(groupId)
    }

    suspend fun addUserToGoing(eventId: Long, userId: Long): Response<Event> {
        val response = onlineRepo.addUserToGoing(eventId, userId)
        if (response.isSuccessful) {
            response.body()?.let { event ->
                offlineRepo.updateEvent(event)
            }
        }
        return response
    }

    suspend fun addUserToMaybe(eventId: Long, userId: Long): Response<Event> {
        val response = onlineRepo.addUserToMaybe(eventId, userId)
        if (response.isSuccessful) {
            response.body()?.let { event ->
                offlineRepo.updateEvent(event)
            }
        }
        return response
    }

    suspend fun addUserToCantGo(eventId: Long, userId: Long): Response<Event> {
        val response = onlineRepo.addUserToCantGo(eventId, userId)
        if (response.isSuccessful) {
            response.body()?.let { event ->
                offlineRepo.updateEvent(event)
            }
        }
        return response
    }

    suspend fun fetchEventAttendees(eventId: Long) {
        val goingUsers = onlineRepo.getGoingUsers(eventId).body() ?: emptyList()
        val maybeUsers = onlineRepo.getMaybeUsers(eventId).body() ?: emptyList()
        val cantGoUsers = onlineRepo.getCantGoUsers(eventId).body() ?: emptyList()

        // Convert those List<User> into lists of user IDs for local storage
        val updatedEvent = offlineRepo.getEventById(eventId)?.apply {
            this.going = goingUsers.map { it.id }
            this.maybe = maybeUsers.map { it.id }
            this.cantGo = cantGoUsers.map { it.id }
        }

        updatedEvent?.let { offlineRepo.updateEvent(it) }
    }

    suspend fun postComment(eventId: Long, userId: Long, comment: String): Response<Comment> {
        return onlineRepo.postComment(eventId, userId, comment)
    }

    suspend fun fetchEventComments(eventId: Long): List<Comment> {
        return onlineRepo.getEventComments(eventId).body() ?: emptyList()
    }

}
