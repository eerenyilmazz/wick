package com.wick.feature.matches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wick.core.data.config.ConfigRepository
import com.wick.core.data.event.AppEventBus
import com.wick.core.data.invitation.InvitationRepository
import com.wick.core.data.match.MatchListRepository
import com.wick.core.data.user.ProfileRepository
import com.wick.core.model.AppEvent
import com.wick.core.model.AppResult
import com.wick.core.model.Invitation
import com.wick.core.model.InvitationStatus
import com.wick.core.model.MatchSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MatchesViewModel @Inject constructor(
    private val matchListRepository: MatchListRepository,
    private val invitationRepository: InvitationRepository,
    private val profileRepository: ProfileRepository,
    private val configRepository: ConfigRepository,
    private val appEventBus: AppEventBus,
) : ViewModel() {

    @Volatile
    private var currentUserId: String? = null

    private val _uiState = MutableStateFlow(MatchesUiState())
    val uiState: StateFlow<MatchesUiState> = _uiState.asStateFlow()

    init {
        loadAll()
        viewModelScope.launch {
            val result = configRepository.getConfig()
            if (result is AppResult.Success) {
                _uiState.value = _uiState.value.copy(
                    extraInvitationTokenCost = result.data.extraInvitationTokenCost,
                    maxRetryInvitations = result.data.maxRetryInvitations,
                )
            }
        }
        viewModelScope.launch {
            appEventBus.events.collect { event ->
                when (event) {
                    AppEvent.MatchCreated,
                    AppEvent.InvitationReceived,
                    AppEvent.InvitationDeclined,
                    AppEvent.SessionStarted,
                    AppEvent.SessionLocked -> loadAll()
                    AppEvent.MessageReceived -> Unit
                }
            }
        }
    }

    fun retry() = loadAll()

    fun sendInvitation(matchId: String, useToken: Boolean = false) {
        if (_uiState.value.invitationStates[matchId] is InvitationSendState.Sending) return

        viewModelScope.launch {
            updateInvitationState(matchId, InvitationSendState.Sending)
            when (val result = invitationRepository.sendInvitation(matchId, useToken)) {
                is AppResult.Success -> {
                    updateInvitationState(matchId, InvitationSendState.Sent(result.data))
                    _uiState.value = _uiState.value.copy(pendingPaidInviteMatchId = null)
                }
                is AppResult.Failure -> {
                    if (result.error == com.wick.core.model.UiError.RateLimited && !useToken) {
                        updateInvitationState(matchId, InvitationSendState.Idle)
                        _uiState.value = _uiState.value.copy(pendingPaidInviteMatchId = matchId)
                    } else {
                        updateInvitationState(matchId, InvitationSendState.Failed(result.error))
                        _uiState.value = _uiState.value.copy(pendingPaidInviteMatchId = null)
                    }
                }
            }
        }
    }

    fun dismissPaidInviteSheet() {
        _uiState.value = _uiState.value.copy(pendingPaidInviteMatchId = null)
    }

    fun confirmPaidInvite(matchId: String) {
        _uiState.value = _uiState.value.copy(pendingPaidInviteMatchId = null)
        sendInvitation(matchId, useToken = true)
    }

    fun acceptInvitation(invitationId: String) {
        respondToInvitation(invitationId) { invitationRepository.acceptInvitation(invitationId) }
    }

    fun declineInvitation(invitationId: String) {
        respondToInvitation(invitationId) { invitationRepository.declineInvitation(invitationId) }
    }

    fun cancelInvitation(invitationId: String, matchId: String) {
        if (invitationId in _uiState.value.cancellingInvitationIds) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                cancellingInvitationIds = _uiState.value.cancellingInvitationIds + invitationId,
            )
            val result = invitationRepository.cancelInvitation(invitationId)
            _uiState.value = _uiState.value.copy(
                cancellingInvitationIds = _uiState.value.cancellingInvitationIds - invitationId,
                invitationStates = if (result is AppResult.Success) {
                    _uiState.value.invitationStates + (matchId to InvitationSendState.Idle)
                } else {
                    _uiState.value.invitationStates
                },
                error = if (result is AppResult.Failure) result.error else _uiState.value.error,
            )
        }
    }

    private fun respondToInvitation(invitationId: String, action: suspend () -> AppResult<Invitation>) {
        if (invitationId in _uiState.value.respondingInvitationIds) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                respondingInvitationIds = _uiState.value.respondingInvitationIds + invitationId,
            )
            val result = action()
            _uiState.value = _uiState.value.copy(
                respondingInvitationIds = _uiState.value.respondingInvitationIds - invitationId,
                incomingInvitations = if (result is AppResult.Success) {
                    _uiState.value.incomingInvitations.filterNot { it.invitation.id == invitationId }
                } else {
                    _uiState.value.incomingInvitations
                },
                invitationStates = if (result is AppResult.Success) {
                    _uiState.value.invitationStates + (result.data.matchId to result.data.toSendState(currentUserId))
                } else {
                    _uiState.value.invitationStates
                },
                error = if (result is AppResult.Failure) result.error else _uiState.value.error,
            )
        }
    }

    fun unmatch(matchId: String, onSuccess: () -> Unit) {
        if (matchId in _uiState.value.unmatchingMatchIds) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                unmatchingMatchIds = _uiState.value.unmatchingMatchIds + matchId,
            )
            val result = matchListRepository.unmatch(matchId)
            _uiState.value = _uiState.value.copy(
                unmatchingMatchIds = _uiState.value.unmatchingMatchIds - matchId,
                matches = if (result is AppResult.Success) {
                    _uiState.value.matches.filterNot { it.id == matchId }
                } else {
                    _uiState.value.matches
                },
                error = if (result is AppResult.Failure) result.error else _uiState.value.error,
            )
            if (result is AppResult.Success) {
                onSuccess()
            }
        }
    }

    private fun updateInvitationState(matchId: String, state: InvitationSendState) {
        _uiState.value = _uiState.value.copy(
            invitationStates = _uiState.value.invitationStates + (matchId to state),
        )
    }

    private fun loadAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            if (currentUserId == null) {
                val userResult = profileRepository.getCurrentUser()
                if (userResult is AppResult.Success) {
                    currentUserId = userResult.data.id
                }
            }
            when (val result = matchListRepository.getMatches()) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        matches = result.data,
                        invitationStates = _uiState.value.invitationStates + seedInvitationStates(result.data),
                    )
                    loadIncomingInvitations(result.data)
                }

                is AppResult.Failure -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.error,
                )
            }
        }
    }

    private fun seedInvitationStates(matches: List<MatchSummary>): Map<String, InvitationSendState> {
        val myId = currentUserId
        return matches.mapNotNull { match ->
            val invitation = match.latestInvitation ?: return@mapNotNull null
            val state = invitation.toSendState(myId)
            match.id to state
        }.toMap()
    }

    private fun Invitation.toSendState(myId: String?): InvitationSendState = when (status) {
        InvitationStatus.ACCEPTED -> InvitationSendState.Accepted(this)
        InvitationStatus.DECLINED -> InvitationSendState.Declined(this)
        InvitationStatus.CANCELLED -> InvitationSendState.Idle
        InvitationStatus.PENDING -> if (senderId == myId) {
            InvitationSendState.Sent(this)
        } else {
            InvitationSendState.IncomingPending(this)
        }
    }

    private suspend fun loadIncomingInvitations(matches: List<MatchSummary>) {
        when (val result = invitationRepository.getIncomingInvitations()) {
            is AppResult.Success -> {
                val matchesById = matches.associateBy { it.id }
                _uiState.value = _uiState.value.copy(
                    incomingInvitations = result.data.mapNotNull { invitation ->
                        matchesById[invitation.matchId]?.let { match ->
                            IncomingInvitation(invitation = invitation, fromUser = match.otherUser)
                        }
                    },
                )
            }

            is AppResult.Failure -> _uiState.value = _uiState.value.copy(
                error = result.error,
            )
        }
    }
}
