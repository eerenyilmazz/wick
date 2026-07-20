package com.wick.app.navigation

import kotlinx.serialization.Serializable

sealed interface WickRoute {
    @Serializable
    data object Discover : WickRoute

    @Serializable
    data object Matches : WickRoute

    @Serializable
    data object Conversations : WickRoute

    @Serializable
    data object ConnectionError : WickRoute

    @Serializable
    data object Maintenance : WickRoute

    @Serializable
    data object ForceUpdate : WickRoute
}
