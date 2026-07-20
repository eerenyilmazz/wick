package com.wick.feature.matches

import androidx.compose.runtime.Immutable
import com.wick.core.model.Invitation
import com.wick.core.model.MatchSummary
import com.wick.core.model.UiError
import com.wick.core.model.User

@Immutable
data class MatchesUiState(
    val isLoading: Boolean = true,
    val matches: List<MatchSummary> = emptyList(),
    val incomingInvitations: List<IncomingInvitation> = emptyList(),
    val error: UiError? = null,
    val invitationStates: Map<String, InvitationSendState> = emptyMap(),
    val respondingInvitationIds: Set<String> = emptySet(),
    val cancellingInvitationIds: Set<String> = emptySet(),
    val unmatchingMatchIds: Set<String> = emptySet(),
    val pendingPaidInviteMatchId: String? = null,
    val extraInvitationTokenCost: Int = 1,
    val maxRetryInvitations: Int = 2,
)

data class IncomingInvitation(
    val invitation: Invitation,
    val fromUser: User,
)

sealed interface InvitationSendState {
    data object Idle : InvitationSendState
    data object Sending : InvitationSendState
    data class Sent(val invitation: Invitation) : InvitationSendState
    data class Failed(val error: UiError) : InvitationSendState
    data class Accepted(val invitation: Invitation) : InvitationSendState
    data class Declined(val invitation: Invitation) : InvitationSendState
    data class IncomingPending(val invitation: Invitation) : InvitationSendState
}
