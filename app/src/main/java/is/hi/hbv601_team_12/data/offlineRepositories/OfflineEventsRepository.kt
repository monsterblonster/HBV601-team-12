package `is`.hi.hbv601_team_12.data.offlineRepositories

import `is`.hi.hbv601_team_12.data.dao.EventDao
import `is`.hi.hbv601_team_12.data.entities.Event
import `is`.hi.hbv601_team_12.data.entities.EventParticipant
import `is`.hi.hbv601_team_12.data.entities.ParticipantStatus
import `is`.hi.hbv601_team_12.data.entities.User
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class OfflineEventsRepository(private val eventDao: EventDao) {

    suspend fun createEvent(
        name: String,
        description: String?,
        startDateTime: LocalDateTime,
        durationMinutes: Int,
        creatorId: Int,
        location: String?,
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
            maxParticipants = maxParticipants
        )
        return eventDao.insertEvent(event)
    }
    
    suspend fun updateEvent(event: Event) {
        eventDao.updateEvent(event)
    }
    
    suspend fun deleteEvent(event: Event) {
        eventDao.deleteEvent(event)
    }
    
    suspend fun getEventById(eventId: Int): Event? {
        return eventDao.getEventById(eventId)
    }
    
    fun getAllEvents(): Flow<List<Event>> {
        return eventDao.getAllEvents()
    }
    
    fun getEventsByCreator(userId: Int): Flow<List<Event>> {
        return eventDao.getEventsByCreator(userId)
    }
    
    suspend fun addParticipant(eventId: Int, userId: Int, status: ParticipantStatus = ParticipantStatus.INVITED) {
        val participant = EventParticipant(
            eventId = eventId,
            userId = userId,
            status = status,
        )
        eventDao.addParticipant(participant)
    }
    
    suspend fun removeParticipant(eventId: Int, userId: Int) {
        eventDao.removeParticipantByIds(eventId, userId)
    }
    
    suspend fun getParticipantsForEvent(eventId: Int): List<EventParticipant> {
        return eventDao.getParticipantsForEvent(eventId)
    }
    
    suspend fun getUsersForEvent(eventId: Int): List<User> {
        return eventDao.getUsersForEvent(eventId)
    }
    
    fun getEventsForUser(userId: Int): Flow<List<Event>> {
        return eventDao.getEventsForUser(userId)
    }
    
    suspend fun isUserParticipating(eventId: Int, userId: Int): Boolean {
        return eventDao.isUserParticipating(eventId, userId)
    }
}
