package `is`.hi.hbv601_team_12.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "invitations")
data class Invitation(

    @PrimaryKey(autoGenerate = true)
    val localId: Int = 0,

    @SerializedName("id")
    val serverId: Long? = null,

    val userId: Long? = null,
    val groupId: Long? = null,

    val groupName: String? = null
)
