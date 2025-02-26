package `is`.hi.hbv601_team_12.data.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import `is`.hi.hbv601_team_12.data.entities.LogEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface LogEntryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE) // Breyta í REPLACE?
    suspend fun insert(logEntry: LogEntry)

    @Update
    suspend fun update(logEntry: LogEntry)

    @Delete
    suspend fun delete(logEntry: LogEntry)

    @Query("SELECT * from log_entries WHERE id = :id")
    fun getLogEntry(id: Int): Flow<LogEntry>

    @Query("SELECT * from log_entries ORDER BY id ASC")
    fun getAllLogEntries(): Flow<List<LogEntry>>
    // Þarf líklega fleiri query-ur
}