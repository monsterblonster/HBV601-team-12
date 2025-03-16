package `is`.hi.hbv601_team_12.data.offlineRepositories

import `is`.hi.hbv601_team_12.data.daos.GroupDao
import `is`.hi.hbv601_team_12.data.entities.Group
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class OfflineGroupsRepository(
    private val groupDao: GroupDao
) {

    fun getAllGroupsStream(): Flow<List<Group>> = groupDao.getAllGroups()

    fun getGroupStream(id: Long): Flow<Group?> = flow {
        emit(groupDao.getGroup(id))
    }

    fun getGroupsByAdmin(adminId: Long): Flow<List<Group>> = groupDao.getGroupsByAdmin(adminId)

    suspend fun getGroupById(groupId: Long): Group? {
        return groupDao.getGroupById(groupId)
    }

    suspend fun insertGroup(group: Group) {
        groupDao.insert(group)
    }

    suspend fun updateGroup(group: Group) {
        groupDao.update(group)
    }

    suspend fun deleteGroup(group: Group) {
        groupDao.delete(group)
    }

    suspend fun updateGroupMembers(groupId: Long, newMembers: List<Long>) {
        val existingGroup = groupDao.getGroup(groupId)
        if (existingGroup != null) {
            val updatedGroup = existingGroup.updateMembers(newMembers)
            groupDao.update(updatedGroup)
        }
    }
}
