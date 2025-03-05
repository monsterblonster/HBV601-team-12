package `is`.hi.hbv601_team_12.data

import android.content.Context
import `is`.hi.hbv601_team_12.data.offlineRepositories.*
import `is`.hi.hbv601_team_12.data.repositories.*

interface AppContainer {
    val commentsRepository: CommentsRepository
    val eventsRepository: EventsRepository
    val groupsRepository: GroupsRepository
    val invitationsRepository: InvitationsRepository
    val logEntriesRepository: LogEntriesRepository
    val tagsRepository: TagsRepository
    val usersRepository: UsersRepository
}

class AppDataContainer(private val context: Context) : AppContainer {

    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    override val commentsRepository: CommentsRepository by lazy {
        OfflineCommentsRepository(database.commentDao())
    }

    override val invitationsRepository: InvitationsRepository by lazy {
        OfflineInvitationsRepository(database.invitationDao())
    }

    override val logEntriesRepository: LogEntriesRepository by lazy {
        OfflineLogEntriesRepository(database.logEntryDao())
    }

    override val tagsRepository: TagsRepository by lazy {
        OfflineTagsRepository(database.tagDao())
    }

    override val groupsRepository: GroupsRepository by lazy {
        OfflineGroupsRepository(database.groupDao())
    }

    override val usersRepository: UsersRepository by lazy {
        OfflineUsersRepository(database.userDao())
    }

    override val eventsRepository: EventsRepository by lazy {
        OfflineEventsRepository(database.eventDao())
    }
}
