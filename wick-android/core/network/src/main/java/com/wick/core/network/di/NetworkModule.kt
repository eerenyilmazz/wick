package com.wick.core.network.di

import com.wick.core.network.auth.AuthApi
import com.wick.core.network.notification.InboxNotificationApi
import com.wick.core.network.notification.NotificationPreferencesApi
import com.wick.core.network.auth.AuthInterceptor
import com.wick.core.network.auth.TokenAuthenticator
import com.wick.core.network.config.AppConfigApi
import com.wick.core.network.invitation.InvitationApi
import com.wick.core.network.legal.LegalApi
import com.wick.core.network.location.LocationApi
import com.wick.core.network.match.MatchApi
import com.wick.core.network.payment.PaymentApi
import com.wick.core.network.photo.PhotoApi
import com.wick.core.network.safety.SafetyApi
import com.wick.core.network.session.ChatSocketClient
import com.wick.core.network.session.KrossbowChatSocketClient
import com.wick.core.network.session.SessionApi
import com.wick.core.network.token.TokenApi
import com.wick.core.network.user.UserApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CertPin

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        @BaseUrl baseUrl: String,
        @CertPin certPin: String,
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val builder = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .authenticator(tokenAuthenticator)
        if (certPin.isNotEmpty()) {
            val host = runCatching { baseUrl.toHttpUrl().host }.getOrNull() ?: ""
            if (host.isNotEmpty()) {
                builder.certificatePinner(
                    okhttp3.CertificatePinner.Builder().add(host, certPin).build(),
                )
            }
        }
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        @BaseUrl baseUrl: String,
        okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideLegalApi(retrofit: Retrofit): LegalApi = retrofit.create(LegalApi::class.java)

    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi = retrofit.create(UserApi::class.java)

    @Provides
    @Singleton
    fun provideAppConfigApi(retrofit: Retrofit): AppConfigApi = retrofit.create(AppConfigApi::class.java)

    @Provides
    @Singleton
    fun providePhotoApi(retrofit: Retrofit): PhotoApi = retrofit.create(PhotoApi::class.java)

    @Provides
    @Singleton
    fun provideLocationApi(retrofit: Retrofit): LocationApi = retrofit.create(LocationApi::class.java)

    @Provides
    @Singleton
    fun provideMatchApi(retrofit: Retrofit): MatchApi = retrofit.create(MatchApi::class.java)

    @Provides
    @Singleton
    fun provideSafetyApi(retrofit: Retrofit): SafetyApi = retrofit.create(SafetyApi::class.java)

    @Provides
    @Singleton
    fun provideInvitationApi(retrofit: Retrofit): InvitationApi = retrofit.create(InvitationApi::class.java)

    @Provides
    @Singleton
    fun providePaymentApi(retrofit: Retrofit): PaymentApi = retrofit.create(PaymentApi::class.java)

    @Provides
    @Singleton
    fun provideTokenApi(retrofit: Retrofit): TokenApi = retrofit.create(TokenApi::class.java)

    @Provides
    @Singleton
    fun provideSessionApi(retrofit: Retrofit): SessionApi = retrofit.create(SessionApi::class.java)

    @Provides
    @Singleton
    fun provideChatSocketClient(impl: KrossbowChatSocketClient): ChatSocketClient = impl

    @Provides
    @Singleton
    fun provideNotificationPreferencesApi(retrofit: Retrofit): NotificationPreferencesApi =
        retrofit.create(NotificationPreferencesApi::class.java)

    @Provides
    @Singleton
    fun provideInboxNotificationApi(retrofit: Retrofit): InboxNotificationApi =
        retrofit.create(InboxNotificationApi::class.java)
}
