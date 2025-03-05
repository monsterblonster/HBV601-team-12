package `is`.hi.hbv601_team_12.data.offlineRepositories

import `is`.hi.hbv601_team_12.data.daos.EventDao
import `is`.hi.hbv601_team_12.data.entities.Event
import `is`.hi.hbv601_team_12.data.repositories.EventsRepository
import kotlinx.coroutines.flow.Flow


class OfflineEventsRepository(private val eventDao: EventDao) : EventsRepository {
    override fun getAllEventsStream(): Flow<List<Event>> {
        return eventDao.getAllEvents()
    }

    override fun getEventStream(id: Int): Flow<Event> {
        return eventDao.getEvent(id)
    }

    override suspend fun insertEvent(event: Event) {
        return eventDao.insert(event)
    }

    override suspend fun deleteEvent(event: Event) {
        return eventDao.delete(event)
    }

    override suspend fun updateEvent(event: Event) {
        return eventDao.update(event)
    }
}