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
    @Insert(onConflict = OnConflictStrategy.IGNORE) // Breyta í REPLACE?
    suspend fun insert(invitation: Invitation)

    @Update
    suspend fun update(invitation: Invitation)

    @Delete
    suspend fun delete(invitation: Invitation)

    @Query("SELECT * from invitations WHERE id = :id")
    fun getInvitation(id: Int): Flow<Invitation>

    @Query("SELECT * from invitations ORDER BY id ASC")
    fun getAllInvitations(): Flow<List<Invitation>>
    // Þarf líklega fleiri query-ur
}