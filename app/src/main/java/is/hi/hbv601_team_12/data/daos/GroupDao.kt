package `is`.hi.hbv601_team_12.data.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import `is`.hi.hbv601_team_12.data.entities.Group
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(group: Group)

    @Update
    suspend fun update(group: Group)

    @Delete
    suspend fun delete(group: Group)

    @Query("SELECT * FROM `groups` WHERE id = :id")
    suspend fun getGroup(id: Long): Group?

    @Query("SELECT * FROM `groups` ORDER BY id ASC")
    fun getAllGroups(): Flow<List<Group>>

    @Query("SELECT * FROM `groups` WHERE adminId = :adminId ORDER BY id ASC")
    fun getGroupsByAdmin(adminId: Long): Flow<List<Group>>

    @Query("UPDATE `groups` SET members = :newMembers WHERE id = :groupId")
    suspend fun updateGroupMembers(groupId: Long, newMembers: List<Long>)

    @Query("SELECT * FROM `groups` WHERE id = :groupId")
    suspend fun getGroupById(groupId: Long): Group?

    @Query("SELECT * FROM `groups`")
    suspend fun getAllGroupsOnce(): List<Group>
}
