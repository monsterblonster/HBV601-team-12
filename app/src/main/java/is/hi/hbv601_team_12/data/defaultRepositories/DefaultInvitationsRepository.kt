package `is`.hi.hbv601_team_12.data.defaultRepositories

import `is`.hi.hbv601_team_12.data.entities.Invitation
import `is`.hi.hbv601_team_12.data.models.BasicResponse
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineInvitationsRepository
import `is`.hi.hbv601_team_12.data.onlineRepositories.OnlineInvitationRepository
import `is`.hi.hbv601_team_12.data.repositories.InvitationsRepository
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

class DefaultInvitationsRepository(
    private val offlineRepo: OfflineInvitationsRepository,
    private val onlineRepo: OnlineInvitationRepository
) : InvitationsRepository {

    override fun getAllInvitationsStream(): Flow<List<Invitation>> {
        return offlineRepo.getAllInvitationsStream()
    }

    override fun getInvitationStream(id: Int): Flow<Invitation> {
        return offlineRepo.getInvitationStream(id)
    }

    override suspend fun insertInvitation(invitation: Invitation) {
        offlineRepo.insertInvitation(invitation)
    }

    override suspend fun deleteInvitation(invitation: Invitation) {
        offlineRepo.deleteInvitation(invitation)
    }

    override suspend fun updateInvitation(invitation: Invitation) {
        offlineRepo.updateInvitation(invitation)
    }

    override suspend fun createInvitation(groupId: Long, username: String): Response<Invitation> {
        val response = onlineRepo.createInvitation(groupId, username)
        if (response.isSuccessful) {
            response.body()?.let { newInvite ->
                offlineRepo.insertInvitation(newInvite)
            }
        }
        return response
    }

    override suspend fun getUserInvites(userId: Long): Response<List<Invitation>> {
        val response = onlineRepo.getUserInvites(userId)
        if (response.isSuccessful) {
            response.body()?.forEach { invite ->
                offlineRepo.insertInvitation(invite)
            }
        }
        return response
    }

    override suspend fun acceptInvite(userId: Long, inviteId: Long): Response<BasicResponse> {
        val response = onlineRepo.acceptInvite(userId, inviteId)
        return response
    }

    override suspend fun declineInvite(userId: Long, inviteId: Long): Response<BasicResponse> {
        val response = onlineRepo.declineInvite(userId, inviteId)
        return response
    }
}
