package `is`.hi.hbv601_team_12.data.repositories

import `is`.hi.hbv601_team_12.data.entities.LogEntry
import kotlinx.coroutines.flow.Flow

interface LogEntriesRepository {
    fun getAllLogEntriesStream(): Flow<List<LogEntry>>
    fun getLogEntryStream(id: Int): Flow<LogEntry>
    suspend fun insertLogEntry(logEntry: LogEntry)
    suspend fun deleteLogEntry(logEntry: LogEntry)
    suspend fun updateLogEntry(logEntry: LogEntry)
}