package `is`.hi.hbv601_team_12.data.repositories

import `is`.hi.hbv601_team_12.data.entities.Invitation
import kotlinx.coroutines.flow.Flow

interface InvitationsRepository {
    fun getAllInvitationsStream(): Flow<List<Invitation>>
    fun getInvitationStream(id: Int): Flow<Invitation>
    suspend fun insertInvitation(invitation: Invitation)
    suspend fun deleteInvitation(invitation: Invitation)
    suspend fun updateInvitation(invitation: Invitation)
}