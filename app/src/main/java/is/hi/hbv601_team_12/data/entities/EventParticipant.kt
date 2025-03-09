package `is`.hi.hbv601_team_12.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index


// many to many
@Entity(
    tableName = "event_participants",
    primaryKeys = ["eventId", "userId"], 
    foreignKeys = [
        ForeignKey(
            entity = Event::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("eventId"),
        Index("userId")
    ]
)
data class EventParticipant(
    val eventId: Int,
    val userId: Int,
    val status: ParticipantStatus = ParticipantStatus.INVITED,
)

enum class ParticipantStatus {
    GOING,
    MAYBE,
    INVITED,
    DECLINED
}
