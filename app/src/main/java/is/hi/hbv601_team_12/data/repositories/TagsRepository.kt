package `is`.hi.hbv601_team_12.data.repositories

import `is`.hi.hbv601_team_12.data.entities.Tag
import kotlinx.coroutines.flow.Flow

interface TagsRepository {
    fun getAllTagsStream(): Flow<List<Tag>>
    fun getTagStream(id: Int): Flow<Tag>
    suspend fun insertTag(tag: Tag)
    suspend fun deleteTag(tag: Tag)
    suspend fun updateTag(tag: Tag)
}