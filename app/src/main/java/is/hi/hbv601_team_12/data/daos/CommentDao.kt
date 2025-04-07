package `is`.hi.hbv601_team_12.data.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import `is`.hi.hbv601_team_12.data.entities.Comment
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE) // Breyta í REPLACE?
    suspend fun insert(comment: Comment)

    @Update
    suspend fun update(comment: Comment)

    @Delete
    suspend fun delete(comment: Comment)

    @Query("SELECT * from comments WHERE id = :id")
    fun getComment(id: Int): Flow<Comment>

    @Query("SELECT * from comments ORDER BY id ASC")
    fun getAllComments(): Flow<List<Comment>>

    @Query("SELECT * from comments WHERE eventId = :eventId ORDER BY id ASC")
    fun getCommentsForEvent(eventId: Long): Flow<List<Comment>>

}