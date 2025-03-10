package `is`.hi.hbv601_team_12.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "groups")
data class Group(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String? = null,
    val tags: String? = null,
    val maxMembers: Int = 10,
    val adminId: Int,
    val groupPicture: String? = null,
    val members: String = ""
) {
    fun getMemberList(): List<Int> {
        return if (members.isBlank()) emptyList() else members.split(",").mapNotNull { it.trim().toIntOrNull() }
    }

    fun updateMembers(newMembers: List<Int>): Group {
        return this.copy(members = newMembers.joinToString(","))
    }
}
