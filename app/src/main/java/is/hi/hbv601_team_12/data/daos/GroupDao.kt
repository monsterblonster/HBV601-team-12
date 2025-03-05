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
    @Insert(onConflict = OnConflictStrategy.IGNORE) // Breyta í REPLACE?
    suspend fun insert(group: Group)

    @Update
    suspend fun update(group: Group)

    @Delete
    suspend fun delete(group: Group)

    @Query("SELECT * from groups WHERE id = :id")
    fun getGroup(id: Int): Flow<Group>

    @Query("SELECT * from groups ORDER BY id ASC")
    fun getAllGroups(): Flow<List<Group>>
    // Þarf líklega fleiri query-ur
}