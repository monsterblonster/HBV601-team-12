package `is`.hi.hbv601_team_12.data.offlineRepositories

import `is`.hi.hbv601_team_12.data.daos.GroupDao
import `is`.hi.hbv601_team_12.data.entities.Group
import `is`.hi.hbv601_team_12.data.repositories.GroupsRepository
import kotlinx.coroutines.flow.Flow


class OfflineGroupsRepository(private val groupDao: GroupDao) : GroupsRepository {
    override fun getAllGroupsStream(): Flow<List<Group>> {
        return groupDao.getAllGroups()
    }

    override fun getGroupStream(id: Int): Flow<Group> {
        return groupDao.getGroup(id)
    }

    override suspend fun deleteGroup(group: Group) {
        return groupDao.delete(group)
    }

    override suspend fun insertGroup(group: Group) {
        return groupDao.insert(group)
    }

    override suspend fun updateGroup(group: Group) {
        return groupDao.update(group)
    }
}