package `is`.hi.hbv601_team_12.data.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "groups")
data class Group(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var groupName: String,
    var description: String? = null,
    var tags: List<String> = emptyList(),
    var maxMembers: Int = 10,
    var adminId: Long,
    var profilePicturePath: String? = null,
    var members: List<Long> = emptyList(),
    var allowUserInvites: Boolean = false
) {
    fun updateMembers(newMembers: List<Long>): Group {
        return this.copy(members = newMembers)
    }

    fun updateTags(newTags: List<String>): Group {
        return this.copy(tags = newTags)
    }

    fun addMember(memberId: Long): Group {
        val updatedMembers = members.toMutableList()
        if (!updatedMembers.contains(memberId) && updatedMembers.size < maxMembers) {
            updatedMembers.add(memberId)
        }
        return this.copy(members = updatedMembers)
    }

    fun removeMember(memberId: Long): Group {
        val updatedMembers = members.toMutableList().apply { remove(memberId) }
        return this.copy(members = updatedMembers)
    }

    fun addTag(tag: String): Group {
        val updatedTags = tags.toMutableList()
        if (!updatedTags.contains(tag)) {
            updatedTags.add(tag)
        }
        return this.copy(tags = updatedTags)
    }

    fun removeTag(tag: String): Group {
        val updatedTags = tags.toMutableList().apply { remove(tag) }
        return this.copy(tags = updatedTags)
    }

    fun getMembersList(): List<Long> {
        return members
    }
}
