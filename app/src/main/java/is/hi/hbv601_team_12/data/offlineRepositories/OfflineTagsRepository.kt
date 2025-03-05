package `is`.hi.hbv601_team_12.data.offlineRepositories

import `is`.hi.hbv601_team_12.data.daos.TagDao
import `is`.hi.hbv601_team_12.data.entities.Tag
import `is`.hi.hbv601_team_12.data.repositories.TagsRepository
import kotlinx.coroutines.flow.Flow


class OfflineTagsRepository(private val tagDao: TagDao) : TagsRepository {
    override fun getAllTagsStream(): Flow<List<Tag>> {
        return tagDao.getAllTags()
    }

    override fun getTagStream(id: Int): Flow<Tag> {
        return tagDao.getTag(id)
    }

    override suspend fun deleteTag(tag: Tag) {
        return tagDao.delete(tag)
    }

    override suspend fun insertTag(tag: Tag) {
        return tagDao.insert(tag)
    }

    override suspend fun updateTag(tag: Tag) {
        return tagDao.update(tag)
    }
}