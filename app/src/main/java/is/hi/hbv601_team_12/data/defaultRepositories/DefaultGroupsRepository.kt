package `is`.hi.hbv601_team_12.data.defaultRepositories

import `is`.hi.hbv601_team_12.data.entities.Group
import `is`.hi.hbv601_team_12.data.models.ImageUploadResponse
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineGroupsRepository
import `is`.hi.hbv601_team_12.data.onlineRepositories.OnlineGroupsRepository
import `is`.hi.hbv601_team_12.data.repositories.GroupsRepository
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response

class DefaultGroupsRepository(
    private val offlineRepo: OfflineGroupsRepository,
    private val onlineRepo: OnlineGroupsRepository
) : GroupsRepository {
    override suspend fun createGroup(group: Group, username: String): Response<Group> {
        val response = onlineRepo.createGroup(group, username)
        if (response.isSuccessful) {
            response.body()?.let { createdGroup ->
                offlineRepo.insertGroup(createdGroup)
            }
        }
        return response
    }

    override suspend fun getGroupById(groupId: Long): Response<Group> {
        val response = onlineRepo.getGroup(groupId)
        if (response.isSuccessful) {
            response.body()?.let { fetchedGroup ->
                offlineRepo.insertGroup(fetchedGroup)
            }
        }
        return response
    }

    override suspend fun deleteGroupOnline(groupId: Long): Response<ResponseBody> {
        val response = onlineRepo.deleteGroup(groupId)
        if (response.isSuccessful) {
            offlineRepo.getGroupById(groupId)?.let { offlineRepo.deleteGroup(it) }
        }
        return response
    }

    override suspend fun editGroupOnline(groupId: Long, updatedGroup: Group): Response<Group> {
        val response = onlineRepo.editGroup(groupId, updatedGroup)
        if (response.isSuccessful) {
            response.body()?.let { editedGroup ->
                offlineRepo.updateGroup(editedGroup)
            }
        }
        return response
    }

    override suspend fun removeUserFromGroup(groupId: Long, userId: Long, currentUserId: Long): Response<Group> {
        val response = onlineRepo.removeUserFromGroup(groupId, userId, currentUserId)
        if (response.isSuccessful) {
            response.body()?.let { updatedGroup ->
                offlineRepo.updateGroup(updatedGroup)
            }
        }
        return response
    }

    override suspend fun getGroupUsers(groupId: Long): Response<List<Long>> {
        return onlineRepo.getGroupUsers(groupId)
    }

    override suspend fun getGroupTags(groupId: Long): Response<List<String>> {
        return onlineRepo.getGroupTags(groupId)
    }

    override suspend fun uploadGroupPicture(groupId: Long, imagePart: MultipartBody.Part): Response<ImageUploadResponse> {
        return onlineRepo.uploadGroupPicture(groupId, imagePart)
    }

    override suspend fun pullUserGroupsOnline(userId: Long): Response<List<Group>> {
        val response = onlineRepo.getGroupsForUser(userId)
        if (response.isSuccessful) {
            response.body()?.forEach { group ->
                offlineRepo.insertGroup(group)
            }
        }
        return response
    }

    override fun getAllGroupsStream(): Flow<List<Group>> {
        return offlineRepo.getAllGroupsStream()
    }

    override fun getGroupStream(id: Long): Flow<Group?> {
        return offlineRepo.getGroupStream(id)
    }

    override fun getGroupsByAdmin(adminId: Long): Flow<List<Group>> {
        return offlineRepo.getGroupsByAdmin(adminId)
    }

    override suspend fun insertGroup(group: Group) {
        offlineRepo.insertGroup(group)
    }

    override suspend fun deleteGroupOffline(group: Group) {
        offlineRepo.deleteGroup(group)
    }

    override suspend fun updateGroupOffline(group: Group) {
        offlineRepo.updateGroup(group)
    }

    override suspend fun updateGroupMembersOffline(groupId: Long, newMembers: List<Long>) {
        offlineRepo.updateGroupMembers(groupId, newMembers)
    }
}
