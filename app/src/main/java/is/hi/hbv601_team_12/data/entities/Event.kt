package `is`.hi.hbv601_team_12.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import `is`.hi.hbv601_team_12.data.converters.LocalDateTimeSerializer
import com.google.gson.annotations.JsonAdapter


@Entity(tableName = "events",)
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val groupId: Long,
    var name: String,
    var description: String?,
    @JsonAdapter(LocalDateTimeSerializer::class)
    var startDateTime: LocalDateTime,
    var durationMinutes: Int,
    var creatorId: Long,
    var going: List<Long> = emptyList(),
    var maybe: List<Long> = emptyList(),
    var cantGo: List<Long> = emptyList(),


    //  Optional
    var location: String? = null,
    var isPublic: Boolean = true,
    var maxParticipants: Int? = null
)