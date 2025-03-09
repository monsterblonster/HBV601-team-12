package `is`.hi.hbv601_team_12.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "events",
)
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    var name: String,
    var description: String?,
    
    var startDateTime: LocalDateTime,
    var durationMinutes: Int, 
    
    var creatorId: Int,
    
    var location: String?,
    
    // ekki viss hvort þessi eru must
    var isPublic: Boolean = true,

    var maxParticipants: Int? = null
    
)
