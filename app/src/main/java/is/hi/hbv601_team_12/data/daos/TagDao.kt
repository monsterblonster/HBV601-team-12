package `is`.hi.hbv601_team_12.data.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import `is`.hi.hbv601_team_12.data.entities.Tag
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE) // Breyta í REPLACE?
    suspend fun insert(tag: Tag)

    @Update
    suspend fun update(tag: Tag)

    @Delete
    suspend fun delete(tag: Tag)

    @Query("SELECT * from tags WHERE id = :id")
    fun getTag(id: Int): Flow<Tag>

    @Query("SELECT * from tags ORDER BY id ASC")
    fun getAllTags(): Flow<List<Tag>>
    // Þarf líklega fleiri query-ur
}