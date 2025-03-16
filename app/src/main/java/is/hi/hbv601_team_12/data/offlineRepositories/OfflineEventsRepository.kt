package `is`.hi.hbv601_team_12.data.offlineRepositories

import `is`.hi.hbv601_team_12.data.daos.EventDao
import `is`.hi.hbv601_team_12.data.entities.Event
import `is`.hi.hbv601_team_12.data.entities.EventParticipant
import `is`.hi.hbv601_team_12.data.entities.ParticipantStatus
import `is`.hi.hbv601_team_12.data.entities.User
import `is`.hi.hbv601_team_12.data.repositories.EventsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDateTime

class OfflineEventsRepository(private val eventDao: EventDao) : EventsRepository {

    override fun getAllEventsStream(): Flow<List<Event>> {
        return eventDao.getAllEvents()
    }

    override fun getEventStream(id: Long): Flow<Event> {
        return eventDao.getEventById(id)
    }

    override suspend fun insertEvent(event: Event) {
        eventDao.insertEvent(event)
    }

    override suspend fun updateEvent(event: Event) {
        eventDao.updateEvent(event)
    }

    override suspend fun deleteEvent(event: Event) {
        eventDao.deleteEvent(event)
    }

    suspend fun createEvent(
        name: String,
        description: String?,
        startDateTime: LocalDateTime,
        durationMinutes: Int,
        creatorId: Long,
        location: String?,
        groupId: Long,
        isPublic: Boolean = true,
        maxParticipants: Int? = null
    ): Long {
        val event = Event(
            name = name,
            description = description,
            startDateTime = startDateTime,
            durationMinutes = durationMinutes,
            creatorId = creatorId,
            location = location,
            isPublic = isPublic,
            maxParticipants = maxParticipants,
            groupId = groupId
        )
        return eventDao.insertEvent(event)
    }

    suspend fun getEventById(id: Long): Event? {
        return eventDao.getEventById(id).firstOrNull()
    }

    fun getEventsByCreator(userId: Long): Flow<List<Event>> {
        return eventDao.getEventsByCreator(userId)
    }

    fun getEventsForGroupStream(groupId: Long): Flow<List<Event>> {
        return eventDao.getEventsByGroupIdStream(groupId)
    }

    suspend fun addParticipant(eventId: Long, userId: Long, status: ParticipantStatus = ParticipantStatus.INVITED) {
        val participant = EventParticipant(
            eventId = eventId,
            userId = userId,
            status = status,
        )
        eventDao.addParticipant(participant)
    }

    suspend fun removeParticipant(eventId: Long, userId: Long) {
        eventDao.removeParticipantByIds(eventId, userId)
    }

    suspend fun getParticipantsForEvent(eventId: Long): List<EventParticipant> {
        return eventDao.getParticipantsForEvent(eventId)
    }

    suspend fun getUsersForEvent(eventId: Long): List<User> {
        return eventDao.getUsersForEvent(eventId)
    }

    fun getEventsForUser(userId: Long): Flow<List<Event>> {
        return eventDao.getEventsForUser(userId)
    }

    suspend fun isUserParticipating(eventId: Long, userId: Long): Boolean {
        return eventDao.isUserParticipating(eventId, userId)
    }
}
