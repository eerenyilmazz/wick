package com.wick.core.data.invitation

import com.wick.core.model.AppResult
import com.wick.core.model.Invitation

interface InvitationRepository {
    suspend fun sendInvitation(matchId: String, useToken: Boolean = false): AppResult<Invitation>
    suspend fun getIncomingInvitations(): AppResult<List<Invitation>>
    suspend fun acceptInvitation(id: String): AppResult<Invitation>
    suspend fun declineInvitation(id: String): AppResult<Invitation>
    suspend fun cancelInvitation(id: String): AppResult<Invitation>
}
