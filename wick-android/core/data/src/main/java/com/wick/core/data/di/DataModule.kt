package com.wick.core.data.di

import com.wick.core.data.auth.AuthRepository
import com.wick.core.data.auth.AuthRepositoryImpl
import com.wick.core.data.auth.TokenStorage
import com.wick.core.data.config.ConfigRepository
import com.wick.core.data.config.ConfigRepositoryImpl
import com.wick.core.data.invitation.InvitationRepository
import com.wick.core.data.invitation.InvitationRepositoryImpl
import com.wick.core.data.legal.LegalRepository
import com.wick.core.data.legal.LegalRepositoryImpl
import com.wick.core.data.location.LocationRepository
import com.wick.core.data.location.LocationRepositoryImpl
import com.wick.core.data.match.DiscoverRepository
import com.wick.core.data.match.DiscoverRepositoryImpl
import com.wick.core.data.match.MatchListRepository
import com.wick.core.data.match.MatchListRepositoryImpl
import com.wick.core.data.notification.InboxNotificationRepository
import com.wick.core.data.notification.InboxNotificationRepositoryImpl
import com.wick.core.data.notification.NotificationPreferencesRepository
import com.wick.core.data.notification.NotificationPreferencesRepositoryImpl
import com.wick.core.data.payment.PaymentRepository
import com.wick.core.data.payment.PaymentRepositoryImpl
import com.wick.core.data.photo.PhotoRepository
import com.wick.core.data.photo.PhotoRepositoryImpl
import com.wick.core.data.safety.SafetyRepository
import com.wick.core.data.safety.SafetyRepositoryImpl
import com.wick.core.data.session.SessionRepository
import com.wick.core.data.session.SessionRepositoryImpl
import com.wick.core.data.token.TokenRepository
import com.wick.core.data.token.TokenRepositoryImpl
import com.wick.core.data.user.DiscoveryPreferenceRepository
import com.wick.core.data.user.DiscoveryPreferenceRepositoryImpl
import com.wick.core.data.user.ProfileRepository
import com.wick.core.data.user.ProfileRepositoryImpl
import com.wick.core.network.auth.TokenProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    abstract fun bindLegalRepository(impl: LegalRepositoryImpl): LegalRepository

    @Binds
    abstract fun bindTokenProvider(impl: TokenStorage): TokenProvider

    @Binds
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    abstract fun bindConfigRepository(impl: ConfigRepositoryImpl): ConfigRepository

    @Binds
    abstract fun bindPhotoRepository(impl: PhotoRepositoryImpl): PhotoRepository

    @Binds
    abstract fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository

    @Binds
    abstract fun bindDiscoverRepository(impl: DiscoverRepositoryImpl): DiscoverRepository

    @Binds
    abstract fun bindMatchListRepository(impl: MatchListRepositoryImpl): MatchListRepository

    @Binds
    abstract fun bindSafetyRepository(impl: SafetyRepositoryImpl): SafetyRepository

    @Binds
    abstract fun bindInvitationRepository(impl: InvitationRepositoryImpl): InvitationRepository

    @Binds
    abstract fun bindDiscoveryPreferenceRepository(impl: DiscoveryPreferenceRepositoryImpl): DiscoveryPreferenceRepository

    @Binds
    abstract fun bindPaymentRepository(impl: PaymentRepositoryImpl): PaymentRepository

    @Binds
    abstract fun bindTokenRepository(impl: TokenRepositoryImpl): TokenRepository

    @Binds
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository

    @Binds
    abstract fun bindNotificationPreferencesRepository(impl: NotificationPreferencesRepositoryImpl): NotificationPreferencesRepository

    @Binds
    abstract fun bindInboxNotificationRepository(impl: InboxNotificationRepositoryImpl): InboxNotificationRepository
}
