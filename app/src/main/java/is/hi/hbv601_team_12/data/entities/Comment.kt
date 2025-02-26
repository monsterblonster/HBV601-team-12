package `is`.hi.hbv601_team_12.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comments")
data class Comment (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val commentData: String,
    val commentTime: String //Þurfum bara að sætta okkur við að þýða streng yfir í tíma til að vista
)