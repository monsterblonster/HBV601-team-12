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
    fun getEventById(eventId: Long): Flow<Event>

    @Query("SELECT * FROM events ORDER BY startDateTime")
    fun getAllEvents(): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE creatorId = :userId ORDER BY startDateTime")
    fun getEventsByCreator(userId: Long): Flow<List<Event>>

    @Insert
    suspend fun addParticipant(eventParticipant: EventParticipant)

    @Insert
    suspend fun addParticipants(participants: List<EventParticipant>)

    @Delete
    suspend fun removeParticipant(eventParticipant: EventParticipant)

    @Query("DELETE FROM event_participants WHERE eventId = :eventId AND userId = :userId")
    suspend fun removeParticipantByIds(eventId: Long, userId: Long)

    @Query("SELECT * FROM event_participants WHERE eventId = :eventId")
    suspend fun getParticipantsForEvent(eventId: Long): List<EventParticipant>

    @Query("""
        SELECT u.* FROM users u
        INNER JOIN event_participants ep ON u.id = ep.userId
        WHERE ep.eventId = :eventId
    """)
    suspend fun getUsersForEvent(eventId: Long): List<User>

    @Query("""
        SELECT e.* FROM events e
        INNER JOIN event_participants ep ON e.id = ep.eventId
        WHERE ep.userId = :userId
        ORDER BY e.startDateTime
    """)
    fun getEventsForUser(userId: Long): Flow<List<Event>>

    @Query("SELECT EXISTS(SELECT 1 FROM event_participants WHERE eventId = :eventId AND userId = :userId)")
    suspend fun isUserParticipating(eventId: Long, userId: Long): Boolean

    @Query("SELECT * FROM events WHERE groupId = :groupId")
    fun getEventsByGroupIdStream(groupId: Long): Flow<List<Event>>
}