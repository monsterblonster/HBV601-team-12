package `is`.hi.hbv601_team_12.data.offlineRepositories

import `is`.hi.hbv601_team_12.data.daos.InvitationDao
import `is`.hi.hbv601_team_12.data.entities.Invitation
import `is`.hi.hbv601_team_12.data.repositories.InvitationsRepository
import kotlinx.coroutines.flow.Flow


class OfflineInvitationsRepository(private val invitationDao: InvitationDao) :
    InvitationsRepository {
    override fun getAllInvitationsStream(): Flow<List<Invitation>> {
        return invitationDao.getAllInvitations()
    }

    override fun getInvitationStream(id: Int): Flow<Invitation> {
        return invitationDao.getInvitation(id)
    }

    override suspend fun deleteInvitation(invitation: Invitation) {
        return invitationDao.delete(invitation)
    }

    override suspend fun insertInvitation(invitation: Invitation) {
        return invitationDao.insert(invitation)
    }

    override suspend fun updateInvitation(invitation: Invitation) {
        return invitationDao.update(invitation)
    }
}