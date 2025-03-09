package `is`.hi.hbv601_team_12.data.daos

import androidx.room.*
import `is`.hi.hbv601_team_12.data.entities.Event
import `is`.hi.hbv601_team_12.data.entities.EventParticipant
import `is`.hi.hbv601_team_12.data.entities.User
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Insert
    suspend fun insertEvent(event: Event): Long
    
    @Update
    suspend fun updateEvent(event: Event)
    
    @Delete
    suspend fun deleteEvent(event: Event)
    
    @Query("SELECT * FROM events WHERE id = :eventId")
    fun getEventById(eventId: Int): Flow<Event>
    
    @Query("SELECT * FROM events ORDER BY startDateTime")
    fun getAllEvents(): Flow<List<Event>>
    
    @Query("SELECT * FROM events WHERE creatorId = :userId ORDER BY startDateTime")
    fun getEventsByCreator(userId: Int): Flow<List<Event>>
    
    @Insert
    suspend fun addParticipant(eventParticipant: EventParticipant)
    
    @Delete
    suspend fun removeParticipant(eventParticipant: EventParticipant)
    
    @Query("DELETE FROM event_participants WHERE eventId = :eventId AND userId = :userId")
    suspend fun removeParticipantByIds(eventId: Int, userId: Int)
    
    @Query("SELECT * FROM event_participants WHERE eventId = :eventId")
    suspend fun getParticipantsForEvent(eventId: Int): List<EventParticipant>
    
    @Query("""
        SELECT u.* FROM users u
        INNER JOIN event_participants ep ON u.id = ep.userId
        WHERE ep.eventId = :eventId
    """)
    suspend fun getUsersForEvent(eventId: Int): List<User>
    
    @Query("""
        SELECT e.* FROM events e
        INNER JOIN event_participants ep ON e.id = ep.eventId
        WHERE ep.userId = :userId
        ORDER BY e.startDateTime
    """)
    fun getEventsForUser(userId: Int): Flow<List<Event>>
    
    @Query("SELECT EXISTS(SELECT 1 FROM event_participants WHERE eventId = :eventId AND userId = :userId)")
    suspend fun isUserParticipating(eventId: Int, userId: Int): Boolean
}
