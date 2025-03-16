package `is`.hi.hbv601_team_12.data.repositories

import `is`.hi.hbv601_team_12.data.entities.Event
import kotlinx.coroutines.flow.Flow

interface EventsRepository {
    fun getAllEventsStream(): Flow<List<Event>>
    fun getEventStream(id: Long): Flow<Event>  // Changed from Int to Long
    suspend fun insertEvent(event: Event)
    suspend fun updateEvent(event: Event)
    suspend fun deleteEvent(event: Event)
}
