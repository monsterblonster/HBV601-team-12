package `is`.hi.hbv601_team_12.data.repositories

import `is`.hi.hbv601_team_12.data.entities.Group
import `is`.hi.hbv601_team_12.data.models.ImageUploadResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import kotlinx.coroutines.flow.Flow

interface GroupsRepository {

    suspend fun createGroup(group: Group, username: String): Response<Group>
    suspend fun getGroupById(groupId: Long): Response<Group>
    suspend fun deleteGroupOnline(groupId: Long): Response<ResponseBody>
    suspend fun editGroupOnline(groupId: Long, updatedGroup: Group): Response<Group>
    suspend fun removeUserFromGroup(groupId: Long, userId: Long, currentUser: String): Response<Group>
    suspend fun getGroupUsers(groupId: Long): Response<List<Long>>
    suspend fun getGroupTags(groupId: Long): Response<List<String>>
    suspend fun uploadGroupPicture(groupId: Long, imagePart: MultipartBody.Part): Response<ImageUploadResponse>
    suspend fun pullUserGroupsOnline(userId: Long): Response<List<Group>>

    fun getAllGroupsStream(): Flow<List<Group>>
    fun getGroupStream(id: Long): Flow<Group?>
    fun getGroupsByAdmin(adminId: Long): Flow<List<Group>>

    suspend fun insertGroup(group: Group)
    suspend fun deleteGroupOffline(group: Group)
    suspend fun updateGroupOffline(group: Group)

    suspend fun updateGroupMembersOffline(groupId: Long, newMembers: List<Long>)
}
