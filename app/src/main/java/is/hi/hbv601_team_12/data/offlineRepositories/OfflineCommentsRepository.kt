package `is`.hi.hbv601_team_12.data.offlineRepositories

import `is`.hi.hbv601_team_12.data.daos.CommentDao
import `is`.hi.hbv601_team_12.data.entities.Comment
import `is`.hi.hbv601_team_12.data.repositories.CommentsRepository
import kotlinx.coroutines.flow.Flow

class OfflineCommentsRepository(private val commentDao: CommentDao) : CommentsRepository {
    override fun getAllCommentsStream(): Flow<List<Comment>> {
        return commentDao.getAllComments()
    }

    override fun getCommentStream(id: Int): Flow<Comment> {
        return commentDao.getComment(id)
    }

    override suspend fun insertComment(comment: Comment) {
        return commentDao.insert(comment)
    }

    override suspend fun deleteComment(comment: Comment) {
        return commentDao.delete(comment)
    }

    override suspend fun updateComment(comment: Comment) {
        return commentDao.update(comment)
    }
}