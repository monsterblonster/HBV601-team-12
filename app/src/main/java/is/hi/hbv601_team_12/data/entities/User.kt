package `is`.hi.hbv601_team_12.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var username: String,
    var email: String,
    var password: String,
    var fullName: String,
    var phoneNumber: String?,
    var address: String?,
    var profilePicture: String?
)
