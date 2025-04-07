package `is`.hi.hbv601_team_12.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "comments")
data class Comment (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val commentData: String,
    val commentTime: LocalDateTime? = null,
    val eventId: Long,
    val authorId: Long
)