package `is`.hi.hbv601_team_12.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tags")
data class Tag (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)