package `is`.hi.hbv601_team_12.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invitations")
data class Invitation (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)