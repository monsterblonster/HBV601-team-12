package `is`.hi.hbv601_team_12.data.offlineRepositories

import `is`.hi.hbv601_team_12.data.daos.GroupDao
import `is`.hi.hbv601_team_12.data.entities.Event
import `is`.hi.hbv601_team_12.data.entities.Group
import `is`.hi.hbv601_team_12.data.repositories.GroupsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class OfflineGroupsRepository(private val groupDao: GroupDao) : GroupsRepository {

    override fun getAllGroupsStream(): Flow<List<Group>> {
        return groupDao.getAllGroups()
    }

    override fun getGroupStream(id: Int): Flow<Group?> {
        return flow {
            emit(groupDao.getGroup(id))
        }
    }
    suspend fun getGroupById(groupId: Int): Group? {
        return groupDao.getGroupById(groupId)
    }

    override fun getGroupsByAdmin(adminId: Int): Flow<List<Group>> {
        return groupDao.getGroupsByAdmin(adminId)
    }

    override suspend fun insertGroup(group: Group) {
        return groupDao.insert(group)
    }

    override suspend fun updateGroup(group: Group) {
        return groupDao.update(group)
    }

    override suspend fun deleteGroup(group: Group) {
        return groupDao.delete(group)
    }

    override suspend fun updateGroupMembers(groupId: Int, newMembers: List<Int>) {
        val group = groupDao.getGroup(groupId)
        if (group != null) {
            val updatedGroup = group.updateMembers(newMembers)
            groupDao.update(updatedGroup)
        }
    }
    override suspend fun getEventsForGroup(groupId: Int): List<Event> {
        return groupDao.getEventsForGroup(groupId) 
    }


}
