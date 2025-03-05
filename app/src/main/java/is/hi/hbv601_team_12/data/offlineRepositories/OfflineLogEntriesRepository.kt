package `is`.hi.hbv601_team_12.data.offlineRepositories

import `is`.hi.hbv601_team_12.data.daos.LogEntryDao
import `is`.hi.hbv601_team_12.data.entities.LogEntry
import `is`.hi.hbv601_team_12.data.repositories.LogEntriesRepository
import kotlinx.coroutines.flow.Flow

class OfflineLogEntriesRepository(private val logEntryDao: LogEntryDao) : LogEntriesRepository {
    override fun getAllLogEntriesStream(): Flow<List<LogEntry>> {
        return logEntryDao.getAllLogEntries()
    }

    override fun getLogEntryStream(id: Int): Flow<LogEntry> {
        return logEntryDao.getLogEntry(id)
    }

    override suspend fun deleteLogEntry(logEntry: LogEntry) {
        return logEntryDao.delete(logEntry)
    }

    override suspend fun insertLogEntry(logEntry: LogEntry) {
        return logEntryDao.insert(logEntry)
    }

    override suspend fun updateLogEntry(logEntry: LogEntry) {
        return logEntryDao.update(logEntry)
    }
}