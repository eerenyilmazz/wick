package com.wick.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickMotion
import com.wick.app.ui.ConnectionErrorScreen
import com.wick.app.ui.ForceUpdateScreen
import com.wick.app.ui.MaintenanceModeScreen
import com.wick.feature.auth.forgotpassword.EmailSentRoute
import com.wick.feature.auth.forgotpassword.ForgotPasswordRoute
import com.wick.feature.auth.forgotpassword.ResetPasswordRoute
import com.wick.feature.auth.legal.LegalConsentRoute
import com.wick.feature.auth.permission.LocationPermissionScreen
import com.wick.feature.auth.permission.NotificationPermissionScreen
import com.wick.feature.discover.DiscoverRoute
import com.wick.feature.matches.MatchesRoute
import com.wick.feature.auth.login.LoginRoute
import com.wick.feature.auth.navigation.AuthRoute
import com.wick.feature.auth.register.RegisterRoute
import com.wick.feature.chat.ChatScreenRoute
import com.wick.feature.chat.ConversationsRoute
import com.wick.feature.chat.navigation.ChatRoute
import com.wick.feature.profile.edit.EditProfileRoute
import com.wick.feature.profile.home.AccountSecurityRoute
import com.wick.feature.profile.home.ProfileHomeRoute
import com.wick.feature.profile.verification.AccountVerificationRoute
import com.wick.feature.profile.legal.LegalDocumentsRoute
import com.wick.feature.profile.navigation.ProfileRoute
import com.wick.feature.profile.notifications.NotificationPreferencesRoute
import com.wick.feature.profile.notifications.NotificationsRoute
import com.wick.feature.profile.preview.ProfilePreviewRoute
import com.wick.feature.profile.wallet.TokenWalletRoute
import com.wick.feature.profile.wallet.TransactionHistoryRoute
import com.wick.feature.profile.setup.ProfileSetupBasicRoute
import com.wick.feature.profile.setup.ProfileSetupBioRoute

private fun NavHostController.navigateToTab(route: Any) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.forwardEnter() =
    slideInHorizontally(
        animationSpec = tween(WickMotion.DurationNavTransition, easing = WickMotion.EasingStandard),
        initialOffsetX = { (it * WickMotion.NavSlideFraction).toInt() },
    ) + fadeIn(animationSpec = tween(WickMotion.DurationNavTransition, easing = WickMotion.EasingStandard))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.forwardExit() =
    slideOutHorizontally(
        animationSpec = tween(WickMotion.DurationNavTransition, easing = WickMotion.EasingStandard),
        targetOffsetX = { -(it * WickMotion.NavSlideFraction).toInt() },
    ) + fadeOut(animationSpec = tween(WickMotion.DurationNavTransition, easing = WickMotion.EasingStandard))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.backEnter() =
    slideInHorizontally(
        animationSpec = tween(WickMotion.DurationNavTransition, easing = WickMotion.EasingStandard),
        initialOffsetX = { -(it * WickMotion.NavSlideFraction).toInt() },
    ) + fadeIn(animationSpec = tween(WickMotion.DurationNavTransition, easing = WickMotion.EasingStandard))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.backExit() =
    slideOutHorizontally(
        animationSpec = tween(WickMotion.DurationNavTransition, easing = WickMotion.EasingStandard),
        targetOffsetX = { (it * WickMotion.NavSlideFraction).toInt() },
    ) + fadeOut(animationSpec = tween(WickMotion.DurationNavTransition, easing = WickMotion.EasingStandard))

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun WickNavHost(
    navController: NavHostController = rememberNavController(),
    startDestinationViewModel: StartDestinationViewModel = hiltViewModel(),
) {
    val startDestination by startDestinationViewModel.startDestination.collectAsStateWithLifecycle()

    CompositionLocalProvider(LocalRippleConfiguration provides RippleConfiguration(color = WickColors.Primary)) {
        Surface(
            modifier = Modifier.safeDrawingPadding(),
            color = WickColors.Background,
        ) {
            when (val resolved = startDestination) {
                StartDestination.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = WickColors.Primary)
                }

                StartDestination.Maintenance -> MaintenanceModeScreen()

                StartDestination.ForceUpdate -> ForceUpdateScreen(onUpdate = {})

                else -> WickNavGraph(
                    navController = navController,
                    startDestination = when (resolved) {
                        StartDestination.Discover -> WickRoute.Discover
                        StartDestination.ProfileSetup -> ProfileRoute.SetupBasic
                        else -> AuthRoute.Login()
                    },
                )
            }
        }
    }
}

@Composable
private fun WickNavGraph(
    navController: NavHostController,
    startDestination: Any,
    sessionExpiryViewModel: SessionExpiryViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) {
        sessionExpiryViewModel.sessionExpiredFlow.collect {
            navController.navigate(AuthRoute.Login()) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { forwardEnter() },
        exitTransition = { forwardExit() },
        popEnterTransition = { backEnter() },
        popExitTransition = { backExit() },
    ) {
            composable<AuthRoute.Login> { backStackEntry ->
                val route: AuthRoute.Login = backStackEntry.toRoute()
                LoginRoute(
                    registeredSuccessfully = route.registered,
                    onNavigateToDiscover = {
                        navController.navigate(WickRoute.Discover) {
                            popUpTo<AuthRoute.Login> { inclusive = true }
                        }
                    },
                    onNavigateToProfileSetup = {
                        navController.navigate(ProfileRoute.SetupBasic) {
                            popUpTo<AuthRoute.Login> { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(AuthRoute.Register) { launchSingleTop = true }
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate(AuthRoute.ForgotPassword)
                    },
                )
            }

            composable<AuthRoute.ForgotPassword> {
                ForgotPasswordRoute(
                    onBack = { navController.popBackStack() },
                    onNavigateToEmailSent = { email ->
                        navController.navigate(AuthRoute.EmailSent(email))
                    },
                )
            }

            composable<AuthRoute.EmailSent> { backStackEntry ->
                val route: AuthRoute.EmailSent = backStackEntry.toRoute()
                EmailSentRoute(
                    email = route.email,
                    onBack = { navController.popBackStack() },
                )
            }

            composable<AuthRoute.ResetPassword>(
                deepLinks = listOf(
                    androidx.navigation.navDeepLink {
                        uriPattern = "wickapp://reset-password?token={token}"
                    },
                    androidx.navigation.navDeepLink {
                        uriPattern = "https://wickapp.com/reset-password?token={token}"
                    },
                ),
            ) {
                ResetPasswordRoute(
                    onBack = { navController.popBackStack() },
                    onNavigateToLogin = {
                        navController.navigate(AuthRoute.Login()) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                )
            }

            composable<AuthRoute.Register> {
                RegisterRoute(
                    onNavigateToLegalConsent = { email, password, displayName ->
                        navController.navigate(
                            AuthRoute.LegalConsent(
                                email = email,
                                password = password,
                                displayName = displayName,
                            ),
                        )
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    },
                )
            }

            composable<AuthRoute.LegalConsent> {
                LegalConsentRoute(
                    onNavigateToLogin = {
                        navController.navigate(AuthRoute.Login(registered = true)) {
                            popUpTo<AuthRoute.Login> { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            composable<ProfileRoute.SetupBasic> {
                ProfileSetupBasicRoute(
                    onNavigateToBio = {
                        navController.navigate(ProfileRoute.SetupBio)
                    },
                )
            }

            composable<ProfileRoute.SetupBio> {
                ProfileSetupBioRoute(
                    onNavigateToDiscover = {
                        navController.navigate(WickRoute.Discover) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                )
            }

            composable<WickRoute.Discover> {
                DiscoverRoute(
                    onNavigateToProfile = {
                        navController.navigateToTab(ProfileRoute.Home)
                    },
                    onNavigateToMatches = {
                        navController.navigateToTab(WickRoute.Matches)
                    },
                    onNavigateToConversations = {
                        navController.navigateToTab(WickRoute.Conversations)
                    },
                )
            }

            composable<WickRoute.Matches> {
                MatchesRoute(
                    onNavigateToDiscover = {
                        navController.navigateToTab(WickRoute.Discover)
                    },
                    onNavigateToProfile = {
                        navController.navigateToTab(ProfileRoute.Home)
                    },
                    onNavigateToConversations = {
                        navController.navigateToTab(WickRoute.Conversations)
                    },
                    onNavigateToChat = { matchId, otherUserId, otherDisplayName, otherPhotoUrl ->
                        navController.navigate(ChatRoute(matchId, otherUserId, otherDisplayName, otherPhotoUrl)) { launchSingleTop = true }
                    },
                )
            }

            composable<WickRoute.Conversations> {
                ConversationsRoute(
                    onNavigateToDiscover = {
                        navController.navigateToTab(WickRoute.Discover)
                    },
                    onNavigateToMatches = {
                        navController.navigateToTab(WickRoute.Matches)
                    },
                    onNavigateToProfile = {
                        navController.navigateToTab(ProfileRoute.Home)
                    },
                    onNavigateToChat = { matchId, otherUserId, otherDisplayName, otherPhotoUrl ->
                        navController.navigate(ChatRoute(matchId, otherUserId, otherDisplayName, otherPhotoUrl)) { launchSingleTop = true }
                    },
                )
            }

            composable<ChatRoute> {
                ChatScreenRoute(
                    onBack = { navController.popBackStack() },
                    onBuyTokens = { navController.navigate(ProfileRoute.TokenWallet) { launchSingleTop = true } },
                )
            }

            composable<ProfileRoute.Home> {
                ProfileHomeRoute(
                    onNavigateToDiscover = {
                        navController.navigateToTab(WickRoute.Discover)
                    },
                    onNavigateToMatches = {
                        navController.navigateToTab(WickRoute.Matches)
                    },
                    onNavigateToConversations = {
                        navController.navigateToTab(WickRoute.Conversations)
                    },
                    onNavigateToLegalDocuments = {
                        navController.navigate(ProfileRoute.LegalDocuments) { launchSingleTop = true }
                    },
                    onNavigateToEditProfile = {
                        navController.navigate(ProfileRoute.Edit) { launchSingleTop = true }
                    },
                    onNavigateToPreview = {
                        navController.navigate(ProfileRoute.Preview) { launchSingleTop = true }
                    },
                    onNavigateToTokenWallet = {
                        navController.navigate(ProfileRoute.TokenWallet) { launchSingleTop = true }
                    },
                    onNavigateToLogin = {
                        navController.navigate(AuthRoute.Login()) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToTransactionHistory = {
                        navController.navigate(ProfileRoute.TransactionHistory) { launchSingleTop = true }
                    },
                    onNavigateToNotificationPreferences = {
                        navController.navigate(ProfileRoute.NotificationPreferences) { launchSingleTop = true }
                    },
                    onNavigateToNotifications = {
                        navController.navigate(ProfileRoute.Notifications) { launchSingleTop = true }
                    },
                    onNavigateToAccountSecurity = {
                        navController.navigate(ProfileRoute.AccountSecurity) { launchSingleTop = true }
                    },
                    onNavigateToAccountVerification = {
                        navController.navigate(ProfileRoute.AccountVerification) { launchSingleTop = true }
                    },
                )
            }

            composable<ProfileRoute.TokenWallet> {
                TokenWalletRoute(onBack = { navController.popBackStack() })
            }

            composable<ProfileRoute.LegalDocuments> {
                LegalDocumentsRoute(onBack = { navController.popBackStack() })
            }

            composable<ProfileRoute.Edit> {
                EditProfileRoute(onBack = { navController.popBackStack() })
            }

            composable<ProfileRoute.Preview> {
                ProfilePreviewRoute(onBack = { navController.popBackStack() })
            }

            composable<ProfileRoute.TransactionHistory> {
                TransactionHistoryRoute(onBack = { navController.popBackStack() })
            }

            composable<ProfileRoute.NotificationPreferences> {
                NotificationPreferencesRoute(onBack = { navController.popBackStack() })
            }

            composable<ProfileRoute.Notifications> {
                NotificationsRoute(
                    onBack = { navController.popBackStack() },
                    onNavigateToSettings = { navController.navigate(ProfileRoute.NotificationPreferences) { launchSingleTop = true } },
                    onNavigateToMatches = { navController.navigate(WickRoute.Matches) { launchSingleTop = true } },
                    onNavigateToProfile = { navController.popBackStack() },
                )
            }

            composable<ProfileRoute.AccountSecurity> {
                AccountSecurityRoute(
                    onBack = { navController.popBackStack() },
                    onNavigateToLogin = {
                        navController.navigate(AuthRoute.Login()) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                )
            }

            composable<AuthRoute.LocationPermission> {
                LocationPermissionScreen(
                    onAllow = { navController.popBackStack() },
                    onSkip = { navController.popBackStack() },
                )
            }

            composable<AuthRoute.NotificationPermission> {
                NotificationPermissionScreen(
                    onAllow = { navController.popBackStack() },
                    onSkip = { navController.popBackStack() },
                )
            }

            composable<WickRoute.ConnectionError> {
                ConnectionErrorScreen(
                    onBack = { navController.popBackStack() },
                    onRetry = { navController.popBackStack() },
                )
            }

            composable<WickRoute.Maintenance> {
                MaintenanceModeScreen()
            }

            composable<WickRoute.ForceUpdate> {
                ForceUpdateScreen(
                    onUpdate = { navController.popBackStack() },
                )
            }

            composable<ProfileRoute.AccountVerification> {
                AccountVerificationRoute(onBack = { navController.popBackStack() })
            }
    }
}
