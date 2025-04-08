package `is`.hi.hbv601_team_12.data.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = false)
    val id: Long = 0,
    var userName: String,
    var emailAddress: String,
    var userPW: String,
    var fullName: String,
    var phoneNumber: String?,
    var address: String?,
    var profilePicturePath: String?,
    var groups: List<Long> = emptyList(),
    var ownedEvents: List<Long> = emptyList(),
    var eventsGoing: List<Long> = emptyList(),
){
    @Ignore
    var confirmPassword: String? = null
}
