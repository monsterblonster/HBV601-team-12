package `is`.hi.hbv601_team_12.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "events",
)
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupId: Int,
    var name: String,
    var description: String?,
    var startDateTime: LocalDateTime,
    var durationMinutes: Int, 
    var creatorId: Int,

    //  Optional
    var location: String? = null, 
    var isPublic: Boolean = true,
    var maxParticipants: Int? = null
    
)
