package com.wick.core.model

sealed interface AppEvent {
    data object MatchCreated : AppEvent
    data object InvitationReceived : AppEvent
    data object InvitationDeclined : AppEvent
    data object SessionStarted : AppEvent
    data object SessionLocked : AppEvent
    data object MessageReceived : AppEvent
}
