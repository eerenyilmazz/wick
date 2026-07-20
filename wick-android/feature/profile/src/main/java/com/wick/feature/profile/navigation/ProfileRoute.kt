package com.wick.feature.profile.navigation

import kotlinx.serialization.Serializable

sealed interface ProfileRoute {
    @Serializable
    data object SetupBasic : ProfileRoute

    @Serializable
    data object SetupBio : ProfileRoute

    @Serializable
    data object Home : ProfileRoute

    @Serializable
    data object LegalDocuments : ProfileRoute

    @Serializable
    data object Edit : ProfileRoute

    @Serializable
    data object Preview : ProfileRoute

    @Serializable
    data object TokenWallet : ProfileRoute

    @Serializable
    data object TransactionHistory : ProfileRoute

    @Serializable
    data object NotificationPreferences : ProfileRoute

    @Serializable
    data object Notifications : ProfileRoute

    @Serializable
    data object AccountSecurity : ProfileRoute

    @Serializable
    data object AccountVerification : ProfileRoute
}
