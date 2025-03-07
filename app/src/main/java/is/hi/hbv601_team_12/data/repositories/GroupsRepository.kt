package `is`.hi.hbv601_team_12.data.repositories

import `is`.hi.hbv601_team_12.data.entities.Group
import kotlinx.coroutines.flow.Flow

interface GroupsRepository {
    fun getAllGroupsStream(): Flow<List<Group>>
    fun getGroupStream(id: Int): Flow<Group>
    suspend fun insertGroup(group: Group)
    suspend fun deleteGroup(group: Group)
    suspend fun updateGroup(group: Group)
}