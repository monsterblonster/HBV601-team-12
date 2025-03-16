package `is`.hi.hbv601_team_12.data.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import `is`.hi.hbv601_team_12.data.entities.Invitation
import kotlinx.coroutines.flow.Flow

@Dao
interface InvitationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(invitation: Invitation)

    @Update
    suspend fun update(invitation: Invitation)

    @Delete
    suspend fun delete(invitation: Invitation)

    @Query("SELECT * FROM invitations WHERE localId = :localId")
    fun getInvitationByLocalId(localId: Int): Flow<Invitation>

    @Query("SELECT * FROM invitations WHERE userId = :userId")
    fun getInvitationsByUser(userId: Long): Flow<List<Invitation>>

    @Query("SELECT * FROM invitations WHERE groupId = :groupId")
    fun getInvitationsByGroup(groupId: Long): Flow<List<Invitation>>

    @Query("SELECT * FROM invitations ORDER BY localId ASC")
    fun getAllInvitations(): Flow<List<Invitation>>

}