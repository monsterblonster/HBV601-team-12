package `is`.hi.hbv601_team_12.data

import android.content.Context
import `is`.hi.hbv601_team_12.data.defaultRepositories.*
import `is`.hi.hbv601_team_12.data.offlineRepositories.*
import `is`.hi.hbv601_team_12.data.repositories.*
import `is`.hi.hbv601_team_12.data.onlineRepositories.*

interface AppContainer {
    val commentsRepository: CommentsRepository
    val groupsRepository: GroupsRepository
    val invitationsRepository: InvitationsRepository
    val logEntriesRepository: LogEntriesRepository
    val tagsRepository: TagsRepository
    val usersRepository: UsersRepository
    val eventsRepository: EventsRepository
}

class AppDataContainer(private val context: Context) : AppContainer {

    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    private val offlineUsersRepository = OfflineUsersRepository(database.userDao())
    private val offlineGroupsRepository = OfflineGroupsRepository(database.groupDao())
    private val offlineInvitationsRepository = OfflineInvitationsRepository(database.invitationDao())
    private val offlineCommentsRepository = OfflineCommentsRepository(database.commentDao())
    private val offlineLogEntriesRepository = OfflineLogEntriesRepository(database.logEntryDao())
    private val offlineTagsRepository = OfflineTagsRepository(database.tagDao())
    private val offlineEventsRepository = OfflineEventsRepository(database.eventDao())

    private val onlineUsersRepository = OnlineUsersRepository(offlineUsersRepository)
    private val onlineGroupsRepository = OnlineGroupsRepository()
    private val onlineInvitationRepository = OnlineInvitationRepository()
    private val onlineEventsRepository = OnlineEventsRepository()

    override val usersRepository: UsersRepository by lazy {
        DefaultUsersRepository(
            offlineRepo = offlineUsersRepository,
            onlineRepo = onlineUsersRepository
        )
    }

    override val groupsRepository: GroupsRepository by lazy {
        DefaultGroupsRepository(
            offlineRepo = offlineGroupsRepository,
            onlineRepo = onlineGroupsRepository
        )
    }

    override val invitationsRepository: InvitationsRepository by lazy {
        DefaultInvitationsRepository(
            offlineRepo = offlineInvitationsRepository,
            onlineRepo = onlineInvitationRepository
        )
    }

    override val eventsRepository: EventsRepository by lazy {
        DefaultEventsRepository(
            offlineRepo = offlineEventsRepository,
            onlineRepo = onlineEventsRepository
        )
    }

    override val commentsRepository: CommentsRepository by lazy {
        offlineCommentsRepository
    }

    override val logEntriesRepository: LogEntriesRepository by lazy {
        offlineLogEntriesRepository
    }

    override val tagsRepository: TagsRepository by lazy {
        offlineTagsRepository
    }
}