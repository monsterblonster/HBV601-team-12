package `is`.hi.hbv601_team_12.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "log_entries")
data class LogEntry (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)