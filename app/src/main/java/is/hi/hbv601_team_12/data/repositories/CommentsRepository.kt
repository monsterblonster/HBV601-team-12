package `is`.hi.hbv601_team_12.data.repositories

import `is`.hi.hbv601_team_12.data.entities.Comment
import kotlinx.coroutines.flow.Flow

interface CommentsRepository {
    fun getAllCommentsStream(): Flow<List<Comment>>
    fun getCommentStream(id: Int): Flow<Comment>
    suspend fun insertComment(comment: Comment)
    suspend fun deleteComment(comment: Comment)
    suspend fun updateComment(comment: Comment)
}