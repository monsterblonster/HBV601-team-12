package `is`.hi.hbv601_team_12.data

import android.content.Context
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineCommentsRepository
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineEventsRepository
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineGroupsRepository
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineInvitationsRepository
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineLogEntriesRepository
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineTagsRepository
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineUsersRepository
import `is`.hi.hbv601_team_12.data.repositories.CommentsRepository
import `is`.hi.hbv601_team_12.data.repositories.EventsRepository
import `is`.hi.hbv601_team_12.data.repositories.GroupsRepository
import `is`.hi.hbv601_team_12.data.repositories.InvitationsRepository
import `is`.hi.hbv601_team_12.data.repositories.LogEntriesRepository
import `is`.hi.hbv601_team_12.data.repositories.TagsRepository
import `is`.hi.hbv601_team_12.data.repositories.UsersRepository

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
    override val commentsRepository: CommentsRepository by lazy {
        OfflineCommentsRepository(AppDatabase.getDatabase(context).commentDao())
    }

    override val invitationsRepository: InvitationsRepository by lazy {
        OfflineInvitationsRepository(AppDatabase.getDatabase(context).invitationDao())
    }

    override val logEntriesRepository: LogEntriesRepository by lazy {
        OfflineLogEntriesRepository(AppDatabase.getDatabase(context).logEntryDao())
    }

    override val tagsRepository: TagsRepository by lazy {
        OfflineTagsRepository(AppDatabase.getDatabase(context).tagDao())
    }

    override val groupsRepository: GroupsRepository by lazy {
        OfflineGroupsRepository(AppDatabase.getDatabase(context).groupDao())
    }

    override val usersRepository: UsersRepository by lazy {
        OfflineUsersRepository(AppDatabase.getDatabase(context).userDao())
    }

    override val eventsRepository: EventsRepository by lazy {
        OfflineEventsRepository(AppDatabase.getDatabase(context).eventDao())
    }
}