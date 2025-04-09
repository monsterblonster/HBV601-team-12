package `is`.hi.hbv601_team_12.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

// Changed entity properties to match the database columns on remote repo
@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val groupId: Long,
    var timeCreated: LocalDateTime? = null,
    var date: LocalDateTime? = null,
    var name: String,
    var description: String?,
    var durationMinutes: Int,
    var creatorId: Long,
    var going: List<Long> = emptyList(),
    var maybe: List<Long> = emptyList(),
    var cantGo: List<Long> = emptyList(),
    var invited: List<Long> = emptyList(),
    var comments: List<Long> = emptyList(),


    //  Optional
    var location: String? = null,
    var isPublic: Boolean = true,
    var maxParticipants: Int? = null
)