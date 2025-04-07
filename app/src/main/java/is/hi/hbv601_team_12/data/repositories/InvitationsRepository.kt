package `is`.hi.hbv601_team_12.data.repositories

import `is`.hi.hbv601_team_12.data.entities.Invitation
import `is`.hi.hbv601_team_12.data.models.BasicResponse
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface InvitationsRepository {

    fun getAllInvitationsStream(): Flow<List<Invitation>>
    fun getInvitationStream(id: Int): Flow<Invitation>
    suspend fun insertInvitation(invitation: Invitation)
    suspend fun deleteInvitation(invitation: Invitation)
    suspend fun updateInvitation(invitation: Invitation)

    suspend fun createInvitation(groupId: Long, username: String): Response<Invitation>
    suspend fun getUserInvites(userId: Long): Response<List<Invitation>>
    suspend fun acceptInvite(userId: Long, inviteId: Long): Response<BasicResponse>
    suspend fun declineInvite(userId: Long, inviteId: Long): Response<BasicResponse>
}
