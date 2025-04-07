package `is`.hi.hbv601_team_12.data.offlineRepositories

import `is`.hi.hbv601_team_12.data.daos.InvitationDao
import `is`.hi.hbv601_team_12.data.entities.Invitation
import kotlinx.coroutines.flow.Flow

class OfflineInvitationsRepository(
    private val invitationDao: InvitationDao
) {

    fun getAllInvitationsStream(): Flow<List<Invitation>> {
        return invitationDao.getAllInvitations()
    }

    fun getInvitationStream(id: Int): Flow<Invitation> {
        return invitationDao.getInvitationByLocalId(id)
    }

    suspend fun insertInvitation(invitation: Invitation) {
        invitationDao.insert(invitation)
    }

    suspend fun updateInvitation(invitation: Invitation) {
        invitationDao.update(invitation)
    }

    suspend fun deleteInvitation(invitation: Invitation) {
        invitationDao.delete(invitation)
    }
}
